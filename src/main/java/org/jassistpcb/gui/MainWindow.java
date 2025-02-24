package org.jassistpcb.gui;

import org.jassistpcb.gui.support.Icons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class MainWindow extends JFrame {
    private static MainWindow instance;

    private static PcbDisplayPanel displayPanel;
    private static SlotPanel slotPanel;
    private JSplitPane splitPane;
    JPanel mainPanel;
    private JTabbedPane tabs;
    private String currentWorkFileName;

    private MainWindow() {
        super("JAssist PCB");
        try {
            initialize();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static SlotPanel getGroupsPanel() {
        return slotPanel;
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

    private void initialize() throws NoSuchFieldException {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        WorkManager workManager = new WorkManager();

        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.add(new JMenuItem(openWorkAction));
        fileMenu.add(new JMenuItem(saveWorkAction));
        fileMenu.add(new JMenuItem(saveWorkWith));
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

        slotPanel = new SlotPanel();
        tabs.addTab("Slots", slotPanel);

        splitPane.setRightComponent(tabs);

        displayPanel = new PcbDisplayPanel();

        workManager.addMonitoring(displayPanel, "rotationAngle");
        workManager.addMonitoring(displayPanel, "originalImage");
        workManager.addMonitoring(slotPanel, "newPartHasMounted");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(workManager.isChange()) {
                    int option = JOptionPane.showConfirmDialog(MainWindow.this,
                            "Deseja salvar as alterações antes de fechar?",
                            "Fechar Janela",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        MainWindow.getInstance().saveWorkAction.actionPerformed(null);
                        dispose();
                        System.exit(0);
                    } else if (option == JOptionPane.NO_OPTION) {
                        dispose();
                        System.exit(0);
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        System.out.println("Fechamento cancelado.");
                    }
                } else {
                    dispose();
                    System.exit(0);
                }
            }
        });


        splitPane.setLeftComponent(displayPanel);

        add(splitPane);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);

        setupKeyBindings();
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

            out.writeObject(displayPanel.getRotationAngle());

            System.out.println("Objeto root e imagem serializados e salvos em " + filename);
            slotPanel.setNewPartHasMounted(false);
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
                double rotationAngle = (Double) in.readObject();
                displayPanel.setRotationAngle(rotationAngle);
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



            if(currentWorkFileName!= null && new File(currentWorkFileName).exists()) {
                DefaultMutableTreeNode root = slotPanel.getRoot();
                saveRootToFile(currentWorkFileName, root);
                MainWindow.getInstance().setTitle("JAssist PCB" + " - " + currentWorkFileName);

                return;
            }

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

                DefaultMutableTreeNode root = slotPanel.getRoot();
                saveRootToFile(fileToSave.getAbsolutePath(), root);
                currentWorkFileName = fileToSave.getAbsolutePath();

                MainWindow.getInstance().setTitle("JAssist PCB" + " - " + currentWorkFileName);

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
                slotPanel.loadTreeState(root);

                currentWorkFileName = fileToOpen.getAbsolutePath();
                MainWindow.getInstance().setTitle("JAssist PCB" + " - " + currentWorkFileName);
            }

        }
    };

    public final Action saveWorkWith = new AbstractAction() {
        {
            putValue(Action.SMALL_ICON, Icons.saveFile);
            putValue(Action.NAME, "Save As...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setDialogTitle("Salvar Como...");

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos WORK (*.work)", "work"));

            int userSelection = fileChooser.showSaveDialog(MainWindow.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".work")) {
                    fileToSave = new File(filePath + ".work");
                }

                if (fileToSave.exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(
                            MainWindow.this,
                            "O arquivo já existe. Deseja sobrescrevê-lo?",
                            "Arquivo Existente",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (overwrite != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                DefaultMutableTreeNode root = slotPanel.getRoot();
                saveRootToFile(fileToSave.getAbsolutePath(), root);
                MainWindow.getInstance().setTitle("JAssist PCB" + " - " + fileToSave.getAbsolutePath());

                System.out.println("Cópia do arquivo salva em: " + fileToSave.getAbsolutePath());
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

        }
    };

    private void setupKeyBindings() {
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();

        KeyStroke rotateLeftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke rotateRightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);

        inputMap.put(rotateLeftKey, "rotateLeft");
        inputMap.put(rotateRightKey, "rotateRight");
        inputMap.put(saveKey,  "save");

        actionMap.put("rotateLeft", displayPanel.rotateLeftAction);
        actionMap.put("rotateRight", displayPanel.rotateLeftAction);
        actionMap.put("save", saveWorkAction);
    }

}