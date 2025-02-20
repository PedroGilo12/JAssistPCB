package org.jassistpcb.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private static MainWindow instance;

    private static PcbDisplayPanel displayPanel;
    private static GroupsPanel groupsPanel;
    private JSplitPane splitPane;
    private JTabbedPane tabs;

    private MainWindow() {
        super("JAssist PCB");
        initialize();
    }

    public static GroupsPanel getGroupsPanel() {
        return groupsPanel;
    }

    public static PcbDisplayPanel getDisplayPanel() {
        return displayPanel;
    }

    public static MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("New"));
        fileMenu.add(new JMenuItem("Open"));
        fileMenu.add(new JMenuItem("Save"));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exit"));
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("Undo"));
        editMenu.add(new JMenuItem("Redo"));
        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.LIGHT_GRAY);
        leftPanel.add(new JLabel("Painel Esquerdo"));
        splitPane.setLeftComponent(leftPanel);

        JTabbedPane tabs = new JTabbedPane();

        PartsPanel rightPanel = new PartsPanel();
        tabs.addTab("Parts", rightPanel);

        groupsPanel = new GroupsPanel();
        tabs.addTab("Groups", groupsPanel);

        splitPane.setRightComponent(tabs);

        displayPanel = new PcbDisplayPanel();
        splitPane.setLeftComponent(displayPanel);

        add(splitPane);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
    }

}