package de.byteandbit;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import de.byteandbit.gui.Gui;

import javax.swing.*;

public class Installer {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(Gui::getInstance);
    }
}