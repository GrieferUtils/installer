package dev.l3g7.griefer_utils.gui.screens;

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