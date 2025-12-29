package de.byteandbit;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import de.byteandbit.gui.Gui;

import javax.swing.*;
import java.lang.management.ManagementFactory;

public class Installer {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(Gui::getInstance);
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        long pid = Long.parseLong(jvmName.split("@")[0]);
    }
}