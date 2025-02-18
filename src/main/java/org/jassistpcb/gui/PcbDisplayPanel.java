package org.jassistpcb.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class PcbDisplayPanel extends JPanel {

    private JLabel imageLabel;
    private BufferedImage originalImage;
    private BufferedImage displayedImage;
    private double zoomFactor = 1.0;
    private double maxZoomFactor = 2.0;
    private double minZoomFactor = 0.5;

    private int offsetX = 0;
    private int offsetY = 0;
    private int lastX = 0;
    private int lastY = 0;

    public PcbDisplayPanel() {
        super(new BorderLayout());
        initialize();
    }

    private void initialize() {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "PCB Display Panel",
                TitledBorder.LEFT,
                TitledBorder.TOP
        );
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        setBorder(titledBorder);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    System.out.printf("X = %d, Y = %d\n", e.getX(), e.getY());

                    lastX = e.getX();
                    lastY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    lastX = e.getX();
                    lastY = e.getY();
                }

                updateImage();
            }
        });

        imageLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int deltaX = e.getX() - lastX;
                    int deltaY = e.getY() - lastY;

                    offsetX += deltaX;
                    offsetY += deltaY;

                    lastX = e.getX();
                    lastY = e.getY();

                    updateImage();
                }
            }
        });

        imageLabel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getPreciseWheelRotation() < 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });
    }

    public void centerImageAt(double x, double y) {
        int imageWidth = (int) (originalImage.getWidth() * zoomFactor);
        int imageHeight = (int) (originalImage.getHeight() * zoomFactor);

        System.out.printf("Image Width = %d, Image Height = %d\n", imageWidth, imageHeight);

        offsetX = - (int)(((x / 0.042330) * zoomFactor)) + (int)((double)getWidth() / 2);
        offsetY = - (imageHeight - (int)(((y / 0.042330) * zoomFactor))) + (int)((double)getHeight() / 2);

        System.out.printf("Offset X = %d, Offset Y = %d\n", offsetX, offsetY);

        updateImage();
    }

    public void displayImage(String imagePath) {
        try {
            originalImage = javax.imageio.ImageIO.read(new java.io.File(imagePath));
            displayedImage = originalImage;
            updateImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoomIn() {
        if (zoomFactor >= maxZoomFactor) return;

        double prevZoomFactor = zoomFactor;
        zoomFactor *= 1.1;

        adjustOffsetsForZoom(prevZoomFactor);
        updateImage();
    }

    private void zoomOut() {
        if (zoomFactor <= minZoomFactor) return;

        double prevZoomFactor = zoomFactor;
        zoomFactor /= 1.1;

        adjustOffsetsForZoom(prevZoomFactor);
        updateImage();
    }

    private void adjustOffsetsForZoom(double prevZoomFactor) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double relativeX =  Math.round((centerX - offsetX) / prevZoomFactor);
        double relativeY = Math.round((centerY - offsetY) / prevZoomFactor);

        offsetX = (int) Math.round(centerX - relativeX * zoomFactor);
        offsetY = (int) Math.round(centerY - relativeY * zoomFactor);

        System.out.printf("Offset X = %d, Offset Y = %d\n", offsetX, offsetY);
    }

    private void updateImage() {
        if (originalImage != null) {

            int width = (int) (originalImage.getWidth() * zoomFactor);
            int height = (int) (originalImage.getHeight() * zoomFactor);

            displayedImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = displayedImage.createGraphics();
            g2d.drawImage(originalImage, offsetX, offsetY, width, height, null);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2d.setColor(Color.RED);
            g2d.fillOval(centerX - 5, centerY - 5, 10, 10);

            g2d.dispose();

            imageLabel.setIcon(new ImageIcon(displayedImage));
            imageLabel.repaint();
        }
    }
}
