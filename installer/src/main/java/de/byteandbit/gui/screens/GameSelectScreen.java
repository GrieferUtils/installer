package de.byteandbit.gui.screens;

import de.byteandbit.api.AgentApi;
import de.byteandbit.api.ProductApi;
import de.byteandbit.data.GameInstance;
import de.byteandbit.gui.Gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.byteandbit.Constants.COMMUNICATION_PORT;
import static de.byteandbit.Util.uiText;


public class GameSelectScreen implements Screen {
    JPanel panel;
    private AgentApi agentApi;
    private DefaultListModel<String> listModel;
    private JList<String> gameList;
    private JButton continueButton;
    private JLabel statusLabel;
    private ScheduledExecutorService scheduler;
    private JLabel titleLabel;
    private JComboBox<String> scopeComboBox;
    private String selectedScope;

    @Override
    public JPanel getScreen() {
        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Game list
        listModel = new DefaultListModel<>();
        gameList = new JList<>(listModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (index0 == index1) {
                    GameInstance instance = agentApi.getGameInstances().get(index0);
                    if (!ProductApi.getInstance().canInstallFor(instance, selectedScope) || !instance.isForge()) {
                        return; // block selection
                    }
                }
                super.setSelectionInterval(index0, index1);
            }
        });
        gameList.setCellRenderer(new GameInstanceRenderer());
        gameList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                continueButton.setEnabled(gameList.getSelectedIndex() != -1);
            }
        });
        JScrollPane scrollPane = new JScrollPane(gameList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with status, scope dropdown, and button
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        // Status panel with status label and scope dropdown
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusLabel = new JLabel(uiText("SCANNING_FOR_GAMES"));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Scope dropdown panel (rightmost)
        JPanel scopePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JLabel scopeLabel = new JLabel("Scope ");
        scopeComboBox = new JComboBox<>();
        scopeComboBox.addActionListener(e -> onScopeChanged());
        scopePanel.add(scopeLabel);
        scopePanel.add(scopeComboBox);
        statusPanel.add(scopePanel, BorderLayout.EAST);

        bottomPanel.add(statusPanel, BorderLayout.NORTH);

        continueButton = new JButton(uiText("INSTALL"));
        continueButton.setEnabled(false);
        continueButton.addActionListener(e -> onContinue());
        bottomPanel.add(continueButton, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        gameList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = gameList.getSelectedIndex();
                continueButton.setEnabled(selectedIndex != -1 && isGameInstanceEnabled(selectedIndex));
            }
        });

        return panel;
    }

    public void load() {
        try {
            agentApi = new AgentApi(COMMUNICATION_PORT);

            // Initialize scope dropdown
            List<String> scopes = ProductApi.getInstance().getAvailableScopes();
            SwingUtilities.invokeLater(() -> {
                for (String scope : scopes) {
                    scopeComboBox.addItem(scope);
                }
                if (!scopes.isEmpty()) {
                    selectedScope = scopes.get(0);
                    scopeComboBox.setSelectedIndex(0);
                }
            });

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                agentApi.scan_for_new_game_instances();
                SwingUtilities.invokeLater(this::updateGameList);
            }, 0, 5, TimeUnit.SECONDS);
            SwingUtilities.invokeLater(() -> {
                titleLabel.setText(String.format(uiText("SELECT_GAME_INSTANCE"), ProductApi.getInstance().getProductName()));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onScopeChanged() {
        selectedScope = (String) scopeComboBox.getSelectedItem();
        updateGameList();
    }

    private boolean isGameInstanceEnabled(int index) {
        if (index < 0 || index >= agentApi.getGameInstances().size() || selectedScope == null) {
            return false;
        }
        GameInstance instance = agentApi.getGameInstances().get(index);
        return ProductApi.getInstance().canInstallFor(instance, selectedScope);
    }

    private void updateGameList() {
        int selectedIndex = gameList.getSelectedIndex();
        listModel.clear();

        for (GameInstance instance : agentApi.getGameInstances()) {
            String displayText = String.format("Minecraft %s (%s) at %s",
                    instance.getMcVersion(),
                    instance.isForge() ? "Forge" : "Vanilla",
                    instance.getGameDir());
            listModel.addElement(displayText);
        }

        if (selectedIndex != -1 && selectedIndex < listModel.getSize()) {
            gameList.setSelectedIndex(selectedIndex);
        }

        if (listModel.isEmpty()) {
            statusLabel.setText(uiText("NO_GAMES_FOUND"));
        } else {
            statusLabel.setText(uiText("SCANNING_FOR_GAMES"));
        }

        gameList.repaint();
    }

    private void onContinue() {
        int selectedIndex = gameList.getSelectedIndex();
        if (selectedIndex != -1 && agentApi != null) {
            GameInstance selectedInstance = agentApi.getGameInstances().get(selectedIndex);
            Gui.getInstance().showScreen(new DownloadAndInstallScreen(selectedInstance, selectedScope));
        }
    }

    private void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (agentApi != null) {
            agentApi.close();
        }
    }

    @Override
    public String identifier() {
        return "gameSelect";
    }

    @Override
    public void onOpen() {
        load();
    }

    @Override
    public void onClose() {
        cleanup();
    }

    private class GameInstanceRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            List<GameInstance> instances = agentApi.getGameInstances();
            if (index >= 0 && index < instances.size()) {
                GameInstance instance = instances.get(index);
                boolean canInstall = ProductApi.getInstance().canInstallFor(instance, selectedScope);

                if (canInstall) {
                    if (instance.isForge()) {
                        label.setEnabled(true);
                        label.setForeground(list.getForeground());
                        label.setToolTipText(null);
                    } else {
                        label.setEnabled(false);
                        label.setForeground(Color.GRAY);
                        label.setToolTipText(uiText("FORGE_REQUIRED"));
                    }
                } else {
                    label.setEnabled(false);
                    label.setForeground(Color.GRAY);
                    label.setToolTipText(uiText("VERSION_NOT_COMPATIBLE_WITH_PRODUCT_AND_SCOPE"));
                }
            }
            return label;
        }
    }
}
