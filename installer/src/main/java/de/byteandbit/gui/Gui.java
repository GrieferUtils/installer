package de.byteandbit.gui;

import de.byteandbit.Constants;
import de.byteandbit.gui.screens.LoadingScreen;
import de.byteandbit.gui.screens.Screen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * description missing.
 */
public class Gui extends JFrame {
    private static Gui gui;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    Screen current_screen = null;

    private Gui() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            this.setIconImage(ImageIO.read(Constants.GU_ICON));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load icon.");
        }
        this.setTitle(Constants.PROGRAM_TITLE);
        this.setSize(new Dimension(Constants.PROGRAM_GEOMETRY.width, Constants.PROGRAM_GEOMETRY.height));
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        this.add(cardPanel);
        this.showScreen(new LoadingScreen());
        this.setVisible(true);
    }

    public static Gui getInstance() {
        if (gui == null) gui = new Gui();
        return gui;
    }

    public void errorAndExit(String error, Throwable cause) {
        JOptionPane.showMessageDialog(cardPanel, error);
        cause.printStackTrace();
        System.exit(-1);
    }

    public void showScreen(Screen screen) {
        if (current_screen != null) current_screen.onClose();

        // Check if the component is already in the cardPanel
        boolean alreadyAdded = false;
        for (Component comp : cardPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(screen.identifier())) {
                alreadyAdded = true;
                break;
            }
        }
        if (!alreadyAdded) {
            JPanel panel = screen.getScreen();
            panel.setName(screen.identifier()); // Use name to track which screen is which
            cardPanel.add(panel, screen.identifier());
        }
        screen.onOpen();
        cardLayout.show(cardPanel, screen.identifier());
        current_screen = screen;
    }
}
