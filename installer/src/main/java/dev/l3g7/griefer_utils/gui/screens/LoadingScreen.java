package dev.l3g7.griefer_utils.gui.screens;

import dev.l3g7.griefer_utils.Constants;
import dev.l3g7.griefer_utils.api.AgentApi;
import dev.l3g7.griefer_utils.api.LocalClockApi;
import dev.l3g7.griefer_utils.api.TranslationApi;
import dev.l3g7.griefer_utils.gui.Gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static dev.l3g7.griefer_utils.Util.uiText;
import static dev.l3g7.griefer_utils.Util.ui_wait;

/**
 * Loading screen with logo and status text.
 */
public class LoadingScreen implements Screen {
    private JLabel statusLabel;
    private JPanel panel;

    @Override
    public JPanel getScreen() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        try {
            BufferedImage logo = ImageIO.read(Constants.GU_LOGO);
            Rectangle screenSize = Constants.PROGRAM_GEOMETRY;
            int targetWidth = screenSize.width / 2;
            int targetHeight = screenSize.height / 2;
            double aspectRatio = (double) logo.getWidth() / logo.getHeight();
            if (targetWidth / aspectRatio > targetHeight) {
                targetWidth = (int) (targetHeight * aspectRatio);
            } else {
                targetHeight = (int) (targetWidth / aspectRatio);
            }

            Image scaledLogo = logo.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(Box.createVerticalGlue());
            panel.add(logoLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusLabel = new JLabel("");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(statusLabel);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Update the status text displayed on the loading screen.
     *
     * @param status The new status text to display
     */
    public void updateStatus(String status) {
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        }
    }

    private void load() {
        new Thread(() -> {
            TranslationApi.getInstance().loadLocalLanguage();
            updateStatus(uiText("CHECKING_INTERNET_CONNECTION"));
            ui_wait();
            try {
                new URL("https://github.com/").openStream().close();
            } catch (Exception e) {
                Gui.getInstance().errorAndExit(uiText("NO_GITHUB_ERROR"), e);
            }
            updateStatus(uiText("CHECKING_SYSTEM_TIME"));
            while(!LocalClockApi.is_clock_in_sync()){
                JOptionPane.showMessageDialog(panel, uiText("CLOCK_NOT_IN_SYNC_MESSAGE"), uiText("CLOCK_NOT_IN_SYNC_TITLE"), JOptionPane.WARNING_MESSAGE);
            }
            updateStatus(uiText("SETUP_DEPENDENCIES"));
            ui_wait();
            try {
                AgentApi.init();
            } catch (IOException e) {
                Gui.getInstance().errorAndExit(uiText("NO_DEPENDENCIES_ERROR"), e);
            }
            updateStatus(uiText("SETUP_COMPLETE"));
            ui_wait();
            Gui.getInstance().showScreen(new GameSelectScreen());
        }).start();
    }

    @Override
    public String identifier() {
        return "loading";
    }

    @Override
    public void onOpen() {
        this.load();
    }

    @Override
    public void onClose() {

    }
}
