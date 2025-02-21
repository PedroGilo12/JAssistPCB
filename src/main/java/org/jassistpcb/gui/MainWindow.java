package org.jassistpcb.gui;

import org.jassistpcb.gui.support.Icons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class MainWindow extends JFrame {
    private static MainWindow instance;

    private static PcbDisplayPanel displayPanel;
    private static GroupsPanel groupsPanel;
    private JSplitPane splitPane;
    private JTabbedPane tabs;

    private boolean hasUnsavedChanges = false;

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
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.add(new JMenuItem(openWorkAction));
        fileMenu.add(new JMenuItem(saveWorkAction));
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

    public void saveRootToFile(String filename, DefaultMutableTreeNode root) {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(root);

            if (displayPanel.getOriginalImage() != null) {
                byte[] imageData = displayPanel.imageToBytes(displayPanel.getOriginalImage());
                out.writeObject(imageData);
            } else {
                out.writeObject(null);
            }

            System.out.println("Objeto root e imagem serializados e salvos em " + filename);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public DefaultMutableTreeNode loadRootFromFile(String filename) {
        DefaultMutableTreeNode loadedRoot = null;
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {

            loadedRoot = (DefaultMutableTreeNode) in.readObject();
            System.out.println("Objeto root desserializado de " + filename);

            byte[] imageData = (byte[]) in.readObject();
            if (imageData != null) {
                BufferedImage image = displayPanel.bytesToImage(imageData);
                displayPanel.setOriginalImage(image);
                displayPanel.updateImage();
            }

        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Classe não encontrada");
            c.printStackTrace();
        }
        return loadedRoot;
    }

    public final Action saveWorkAction = new AbstractAction() {
        {
            putValue(Action.SMALL_ICON, Icons.saveFile);
            putValue(Action.NAME, "Save");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setDialogTitle("Salvar Arquivo");

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos WORK (*.work)", "work"));

            int userSelection = fileChooser.showSaveDialog(MainWindow.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".work")) {
                    fileToSave = new File(filePath + ".work");
                }

                DefaultMutableTreeNode root = groupsPanel.getRoot();
                saveRootToFile(fileToSave.getAbsolutePath(), root);

                System.out.println("Arquivo salvo em: " + fileToSave.getAbsolutePath());
            }
        }
    };

    public final Action openWorkAction = new AbstractAction() {
        {
            putValue(Action.SMALL_ICON, Icons.importFile);
            putValue(Action.NAME, "Open");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setDialogTitle("Abrir Arquivo");

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos WORK (*.work)", "work"));

            int userSelection = fileChooser.showOpenDialog(MainWindow.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToOpen = fileChooser.getSelectedFile();

                DefaultMutableTreeNode root = loadRootFromFile(fileToOpen.getAbsolutePath());
                groupsPanel.loadTreeState(root);

                System.out.println("Arquivo aberto: " + fileToOpen.getAbsolutePath());
            }
        }
    };

    public final Action newAction = new AbstractAction() {
        {
            putValue(Action.SMALL_ICON, Icons.file);
            putValue(Action.NAME, "New");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (hasUnsavedChanges) {
                int option = JOptionPane.showConfirmDialog(
                        MainWindow.this,
                        "Deseja salvar as alterações antes de criar um novo projeto?",
                        "Salvar Alterações",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    saveWorkAction.actionPerformed(e); // Salva as alterações
                } else if (option == JOptionPane.CANCEL_OPTION) {
                    return; // Cancela a operação "New"
                }
            }

            groupsPanel.clearTree();
            displayPanel.setOriginalImage(null);
            displayPanel.updateImage();

            hasUnsavedChanges = false; // Reseta o estado de alterações não salvas
            System.out.println("Novo projeto criado.");
        }
    };


}