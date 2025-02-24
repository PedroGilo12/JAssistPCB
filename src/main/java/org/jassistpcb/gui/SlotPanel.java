package org.jassistpcb.gui;

import org.jassistpcb.utils.PcbPart;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlotPanel extends JPanel {

    private Map<String, List<PcbPart>> groupedParts;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private String currentSlot;
    private JPanel currentSlotPanel;
    private JLabel currentSlotLabel;
    private JTree tree;
    private boolean newPartHasMounted = false;
    private JProgressBar workProgressBar;

    public void setNewPartHasMounted(boolean newPartHasMounted) {
        this.newPartHasMounted = newPartHasMounted;
    }

    public SlotPanel() {
        createUI();
    }

    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    public void loadTreeState(DefaultMutableTreeNode newRoot) {
        if (newRoot != null) {
            this.root = newRoot;
            this.treeModel.setRoot(root);
            treeModel.reload();
            updateWorkProgressBar();
            revalidate();
            repaint();
        }
    }

    public void createUI() {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Slots Panel",
                TitledBorder.LEFT,
                TitledBorder.TOP
        );
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        setBorder(titledBorder);
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);

        JButton resetMountedParts = new JButton("Reset Mounted Parts");

        resetMountedParts.addActionListener(e -> {
            for (int i = 0; i < root.getChildCount(); i++) {
                DefaultMutableTreeNode slotNode = (DefaultMutableTreeNode) root.getChildAt(i);
                for (int j = 0; j < slotNode.getChildCount(); j++) {
                    DefaultMutableTreeNode partNode = (DefaultMutableTreeNode) slotNode.getChildAt(j);
                    Object userObject = partNode.getUserObject();

                    if (userObject instanceof CheckBoxNode) {
                        CheckBoxNode checkBoxNode = (CheckBoxNode) userObject;
                        checkBoxNode.setMounted(false);
                    }
                }
            }

            updateWorkProgressBar();

            tree.repaint();
        });
        toolBar.add(resetMountedParts);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        root = new DefaultMutableTreeNode("Slots");

        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                TreePath selectionPath = tree.getSelectionPath();
                assert selectionPath != null;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                Object userObject = node.getUserObject();

                int key = e.getKeyCode();

                if (key == KeyEvent.VK_ENTER) {
                    if (userObject instanceof String) {
                        setCurrentSlot(((String) userObject).split(":")[0].trim());
                    }

                    if (tree.isCollapsed(selectionPath)) {
                        tree.expandPath(selectionPath);
                    } else {
                        tree.collapsePath(selectionPath);
                    }
                }

                if (key == KeyEvent.VK_SPACE) {
                    if (userObject instanceof String) {
                        tree.expandPath(selectionPath);

                        setCurrentSlot(((String) userObject).split(":")[0].trim());

                        DefaultMutableTreeNode nextNode = getNextNode(node);
                        if (nextNode != null) {
                            CheckBoxNode nextCheckBoxNode = (CheckBoxNode) nextNode.getUserObject();
                            double center_x = Double.parseDouble(nextCheckBoxNode.getPart().getCenterX());
                            double center_y = Double.parseDouble(nextCheckBoxNode.getPart().getCenterY());

                            MainWindow.getDisplayPanel().centerImageAt(center_x, center_y);
                        }
                    }

                    if (userObject instanceof CheckBoxNode checkBoxNode) {
                        if (!checkBoxNode.isMounted()) {
                            setNewPartHasMounted(true);
                            checkBoxNode.setMounted(true);
                        }


                        DefaultMutableTreeNode nextNode = getNextNode(node);
                        System.out.println(nextNode);
                        if (nextNode != null) {
                            CheckBoxNode nextCheckBoxNode = (CheckBoxNode) nextNode.getUserObject();
                            double center_x = Double.parseDouble(nextCheckBoxNode.getPart().getCenterX());
                            double center_y = Double.parseDouble(nextCheckBoxNode.getPart().getCenterY());

                            MainWindow.getDisplayPanel().centerImageAt(center_x, center_y);
                        } else {
                            tree.collapsePath(selectionPath.getParentPath());
                        }

                        updateWorkProgressBar();
                    }

                    tree.setSelectionRow(tree.getLeadSelectionRow() + 1);
                    if (tree.getRowCount() > tree.getLeadSelectionRow()) {
                        tree.scrollPathToVisible(tree.getPathForRow(tree.getLeadSelectionRow() + 1));
                    }
                }

                tree.repaint();
            }

            private DefaultMutableTreeNode getNextNode(DefaultMutableTreeNode currentNode) {
                if (currentNode.getChildCount() > 0) {
                    return (DefaultMutableTreeNode) currentNode.getChildAt(0);
                }

                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
                if (parentNode != null) {
                    int index = parentNode.getIndex(currentNode);
                    if (index < parentNode.getChildCount() - 1) {
                        return (DefaultMutableTreeNode) parentNode.getChildAt(index + 1);
                    }
                }

                return null;
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("mouseClicked");
                int x = e.getX();
                int y = e.getY();
                TreePath path = tree.getPathForLocation(x, y);
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object userObject = node.getUserObject();

                    if (userObject instanceof CheckBoxNode) {

                        CheckBoxNode checkBoxNode = (CheckBoxNode) userObject;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            System.out.println("Clique direito detectado!");
                            showContextMenu(e.getX(), e.getY(), checkBoxNode);
                            tree.repaint();
                            return;
                        }

                        checkBoxNode.setMounted(!checkBoxNode.isMounted());
                        setNewPartHasMounted(true);

                        double center_x = Double.parseDouble(checkBoxNode.getPart().getCenterX());
                        double center_y = Double.parseDouble(checkBoxNode.getPart().getCenterY());

                        MainWindow.getDisplayPanel().centerImageAt(center_x, center_y);
                        tree.repaint();

                        updateWorkProgressBar();
                    }
                }
            }

            private void showContextMenu(int x, int y, CheckBoxNode checkBoxNode) {
                JPopupMenu contextMenu = new JPopupMenu();

                JPanel labelPanel = new JPanel(new BorderLayout());
                JLabel label = new JLabel("Adicione um comentário");
                label.setHorizontalAlignment(SwingConstants.LEFT);
                labelPanel.add(label, BorderLayout.WEST);
                contextMenu.add(labelPanel);

                JTextArea textArea = new JTextArea();
                textArea.addKeyListener(new KeyAdapter() {
                   @Override
                   public void keyPressed(KeyEvent e) {
                       super.keyPressed(e);
                       int key = e.getKeyCode();

                       if (key == KeyEvent.VK_ENTER) {
                           checkBoxNode.setComment(textArea.getText());
                           contextMenu.setVisible(false);
                       }
                   }
                });
                textArea.setEditable(true);
                textArea.setPreferredSize(new Dimension(400, 300));
                textArea.setMaximumSize(textArea.getPreferredSize());
                textArea.setMinimumSize(textArea.getPreferredSize());
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setText(checkBoxNode.getComment());

                JMenuItem save = new JMenuItem("Salvar comentário");
                save.addActionListener(e -> {
                    checkBoxNode.setComment(textArea.getText());
                });

                contextMenu.add(new JScrollPane(textArea));
                contextMenu.add(save);

                contextMenu.show(tree, x, y);
            }
        });

        tree.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object userObject = node.getUserObject();
                    if (userObject instanceof CheckBoxNode) {
                        CheckBoxNode checkBoxNode = (CheckBoxNode) userObject;
                        tree.setToolTipText(checkBoxNode.getComment());
                    } else {
                        tree.setToolTipText(null);
                    }
                } else {
                    tree.setToolTipText(null);
                }
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(tree);
        splitPane.setTopComponent(treeScrollPane);

        currentSlotPanel = new JPanel();
        currentSlotPanel.setLayout(new BorderLayout());

        currentSlotLabel = new JLabel(currentSlot, SwingConstants.CENTER);
        currentSlotLabel.setFont(new Font("Arial", Font.PLAIN, 100));

        workProgressBar = new JProgressBar(0, 100);
        workProgressBar.setStringPainted(true);
        workProgressBar.setFont(new Font("Arial", Font.PLAIN, 20));
        workProgressBar.setForeground(new Color(0, 200, 0));
        workProgressBar.setString("0/0");

        currentSlotPanel.add(workProgressBar, BorderLayout.NORTH);
        currentSlotPanel.add(currentSlotLabel, BorderLayout.CENTER);

        splitPane.setBottomComponent(currentSlotPanel);
    }

    public void setCurrentSlot(String currentSlot) {
        this.currentSlot = currentSlot;
        currentSlotLabel.setText(this.currentSlot);
    }

    public void clearTree() {
        root.removeAllChildren();
    }

    public void updateSlots(List<PcbPart> data) {
        groupedParts = new HashMap<>();
        root.removeAllChildren();

        for (PcbPart part : data) {
            String comment = part.getComment();
            groupedParts.computeIfAbsent(comment, k -> new ArrayList<>()).add(part);
        }


        int slotNumber = 1;
        for (Map.Entry<String, List<PcbPart>> entry : groupedParts.entrySet()) {
            String comment = entry.getKey();
            List<PcbPart> parts = entry.getValue();

            DefaultMutableTreeNode slot = new DefaultMutableTreeNode("Slot " + slotNumber + ": " + comment);
            slotNumber++;

            for (PcbPart part : parts) {
                slot.add(new DefaultMutableTreeNode(new CheckBoxNode(part.getDesignator(), false, part)));
                System.out.println(part.getDesignator() + " - " + part.getComment());
            }

            root.add(slot);
        }

        treeModel.reload();

        revalidate();
        repaint();
    }

    private void updateWorkProgressBar() {
        int totalParts = 0;
        int mountedParts = 0;

        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode slotNode = (DefaultMutableTreeNode) root.getChildAt(i);
            totalParts += slotNode.getChildCount();

            for (int j = 0; j < slotNode.getChildCount(); j++) {
                DefaultMutableTreeNode partNode = (DefaultMutableTreeNode) slotNode.getChildAt(j);
                CheckBoxNode checkBoxNode = (CheckBoxNode) partNode.getUserObject();
                if (checkBoxNode.isMounted()) {
                    mountedParts++;
                }
            }
        }

        if (totalParts > 0) {
            int progress = (int) ((double) mountedParts / totalParts * 100);
            workProgressBar.setValue(progress);
            workProgressBar.setString(mountedParts + "/" + totalParts);
        } else {
            workProgressBar.setValue(0);
            workProgressBar.setString("0/0");
        }

        if (mountedParts == totalParts) {
            workProgressBar.setForeground(new Color(0, 200, 0));
        } else {
            workProgressBar.setForeground(Color.ORANGE);
        }
    }
}

class CheckBoxNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private PcbPart part;
    private boolean mounted;
    private String Comment = "";

    public CheckBoxNode(String text, boolean selected, PcbPart part) {
        this.text = text;
        this.mounted = selected;
        this.part = part;
    }

    public String getComment() {
        return Comment;
    }
    public void setComment(String comment) {
        Comment = comment;
    }

    public PcbPart getPart() {
        return part;
    }

    public String getText() {
        return text;
    }

    public boolean isMounted() {
        return mounted;
    }

    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

    @Override
    public String toString() {
        return text;
    }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
    private JPanel panel = new JPanel(new BorderLayout());
    private JCheckBox checkBox = new JCheckBox();
    private JProgressBar progressBar = new JProgressBar();
    private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    public CheckBoxNodeRenderer() {
        panel.add(checkBox, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        progressBar.setStringPainted(true);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        String nodeName = userObject.toString();

        if (node.getChildCount() > 0 && !nodeName.equals("Slots")) {
            int totalParts = node.getChildCount();
            int mountedParts = 0;
            for (int i = 0; i < totalParts; i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                CheckBoxNode childCheckBoxNode = (CheckBoxNode) childNode.getUserObject();
                if (childCheckBoxNode.isMounted()) {
                    mountedParts++;
                }
            }
            int progress = (int) ((double) mountedParts / totalParts * 100);
            progressBar.setValue(progress);
            progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
            progressBar.setString(mountedParts + "/" + totalParts);

            if(mountedParts == totalParts) {
                progressBar.setForeground(new Color(0, 200, 0));
            } else {
                progressBar.setForeground(Color.orange);
            }

            defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            defaultRenderer.setText(userObject.toString());

            panel.setOpaque(false);
            panel.removeAll();
            panel.add(defaultRenderer, BorderLayout.EAST);
            panel.add(progressBar, BorderLayout.CENTER);

            if (selected) {
                panel.setBackground(defaultRenderer.getBackgroundSelectionColor());
                panel.setForeground(defaultRenderer.getTextSelectionColor());
            } else {
                panel.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
                panel.setForeground(defaultRenderer.getTextNonSelectionColor());
            }

            return panel;
        } else {
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode checkBoxNode = (CheckBoxNode) userObject;
                if(!checkBoxNode.getComment().isEmpty()) {
                    checkBox.setBackground(Color.ORANGE);
                } else {
                    checkBox.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
                }
                checkBox.setToolTipText(checkBoxNode.getComment());
                checkBox.setText(checkBoxNode.getText());
                checkBox.setSelected(checkBoxNode.isMounted());
                return checkBox;
            } else {
                return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        }
    }
}
