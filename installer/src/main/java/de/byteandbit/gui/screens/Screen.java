package de.byteandbit.gui.screens;

import javax.swing.*;

/**
 * description missing.
 */
public interface Screen {
    JPanel getScreen();

    String identifier();

    void onOpen();

    void onClose();
}