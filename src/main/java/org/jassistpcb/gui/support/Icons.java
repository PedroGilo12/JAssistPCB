package org.jassistpcb.gui.support;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.util.Objects;

public class Icons {

    public static Icon addPackage = getIcon("/icons/brackets-plus.svg");
    public static Icon addImage = getIcon("/icons/image-plus.svg");

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
