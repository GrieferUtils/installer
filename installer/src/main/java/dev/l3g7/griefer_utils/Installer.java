package dev.l3g7.griefer_utils;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import dev.l3g7.griefer_utils.gui.Gui;

import javax.swing.*;

public class Installer {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(Gui::getInstance);
    }
}