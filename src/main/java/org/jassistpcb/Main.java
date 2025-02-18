package org.jassistpcb;

import javax.swing.*;
import java.awt.*;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import org.jassistpcb.gui.MainWindow;
import org.jassistpcb.gui.PartsPanel;

public class Main {
    public static void main(String[] args) {

        FlatLaf.registerCustomDefaultsSource("themes");
        try {
            UIManager.setLookAndFeel( new FlatIntelliJLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = MainWindow.getInstance();
            mainWindow.setMinimumSize(new Dimension(800, 600));
            mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
            mainWindow.setVisible(true);
        });
    }
}