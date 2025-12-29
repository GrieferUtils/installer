package de.byteandbit.gui.screens;

import de.byteandbit.api.ProductApi;
import de.byteandbit.gui.Gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static de.byteandbit.Util.uiText;

/**
 * Screen for telling the user that the license they entered requires them to set a minecraft user in the control panel first.
 */
public class LicenseNeedsUserScreen implements Screen {
    private JPanel panel;
    private JButton openBrowserButton;
    private JButton tryAgainButton;

    @Override
    public JPanel getScreen() {
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Message label
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; width: 350px;'>" + uiText("LICENSE_NEEDS_USER_MESSAGE") + "</div></html>");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(messageLabel, gbc);

        // Open Browser button
        openBrowserButton = new JButton(uiText("CONTROL_PANEL_BUTTON"));
        openBrowserButton.addActionListener(e -> openBrowser());
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(openBrowserButton, gbc);

        // Try Again button
        tryAgainButton = new JButton(uiText("RETRY_LICENSE_USERNAME"));
        tryAgainButton.addActionListener(e -> tryAgain());
        gbc.gridx = 1;
        panel.add(tryAgainButton, gbc);

        return panel;
    }

    private void openBrowser() {
        try {
            String url = ProductApi.getInstance().getLicenseControlPanelUrl();
            Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryAgain() {
        new Thread(() -> {
            try {
                ProductApi.getInstance().updateLicense();
                if (ProductApi.getInstance().noUsersRegistered()) {
                    JOptionPane.showMessageDialog(panel, uiText("LICENSE_NEEDS_USER_MESSAGE"), uiText("RETRY_LICENSE_USERNAME_ERROR"), JOptionPane.ERROR_MESSAGE);
                } else {
                    Gui.getInstance().showScreen(new GameSelectScreen());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public String identifier() {
        return "license-needs-user";
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }
}
