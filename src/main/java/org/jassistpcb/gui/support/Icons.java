package org.jassistpcb.gui.support;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.util.Objects;

public class Icons {

    public static Icon addPackage = getIcon("/icons/brackets-plus.svg");
    public static Icon addImage = getIcon("/icons/image-plus.svg");
    public static Icon saveFile = getIcon("/icons/save-01.svg");
    public static Icon importFile = getIcon("/icons/import-file.svg");
    public static Icon file = getIcon("/icons/file.svg");
    public static Icon rotateLeft = getIcon("/icons/rotate-left.svg");
    public static Icon rotateRight = getIcon("/icons/rotate-right.svg");

    public static Icon getIcon(String resourceName, int width, int height) {
        if (resourceName.endsWith(".svg")) {
            return new FlatSVGIcon(resourceName.substring(1), width, height);
        }
        else {
            return new ImageIcon(Objects.requireNonNull(Icons.class.getResource(resourceName)));
        }
    }

    public static Icon getIcon(String resourceName) {
        return getIcon(resourceName, 24, 24);
    }
}
