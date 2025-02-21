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

public class GroupsPanel extends JPanel {

    private Map<String, List<PcbPart>> groupedParts;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private String currentGroup;
    private JPanel currentGroupPanel;
    private JLabel currentGroupLabel;

    public GroupsPanel() {
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
            revalidate();
            repaint();
        }
    }

    public void createUI() {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Groups Panel",
                TitledBorder.LEFT,
                TitledBorder.TOP
        );
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        setBorder(titledBorder);
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);


        root = new DefaultMutableTreeNode("Groups");

        treeModel = new DefaultTreeModel(root);
        JTree tree = new JTree(treeModel);
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

                if(key == KeyEvent.VK_ENTER) {
                    if(userObject instanceof String) {
                        setCurrentGroup(((String) userObject).split(":")[0].trim());
                    }

                    if(tree.isCollapsed(selectionPath)) {
                        tree.expandPath(selectionPath);
                    } else {
                        tree.collapsePath(selectionPath);
                    }
                }

                if(key == KeyEvent.VK_SPACE) {
                    if(userObject instanceof String) {
                        tree.expandPath(selectionPath);

                        setCurrentGroup(((String) userObject).split(":")[0].trim());

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
                        checkBoxNode.setMounted(!checkBoxNode.isMounted());

                        double center_x = Double.parseDouble(checkBoxNode.getPart().getCenterX());
                        double center_y = Double.parseDouble(checkBoxNode.getPart().getCenterY());

                        MainWindow.getDisplayPanel().centerImageAt(center_x, center_y);
                        tree.repaint();
                    }
                }
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(tree);
        splitPane.setTopComponent(treeScrollPane);

        currentGroupPanel = new JPanel();
        currentGroupPanel.setLayout(new BorderLayout());

        currentGroupLabel = new JLabel(currentGroup, SwingConstants.CENTER);
        currentGroupLabel.setFont(new Font("Arial", Font.PLAIN, 100));

        currentGroupPanel.add(currentGroupLabel, BorderLayout.CENTER);

        splitPane.setBottomComponent(currentGroupPanel);
    }

    public void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
        currentGroupLabel.setText(this.currentGroup);
    }

    public void clearTree() {
        root.removeAllChildren();
    }

    public void updateGroups(List<PcbPart> data) {
        groupedParts = new HashMap<>();
        root.removeAllChildren();

        for (PcbPart part : data) {
            String comment = part.getComment();
            groupedParts.computeIfAbsent(comment, k -> new ArrayList<>()).add(part);
        }


        int group_number = 0;
        for (Map.Entry<String, List<PcbPart>> entry : groupedParts.entrySet()) {
            String comment = entry.getKey();
            List<PcbPart> parts = entry.getValue();

            DefaultMutableTreeNode group = new DefaultMutableTreeNode("Group " + group_number + ": " +comment);
            group_number++;

            for (PcbPart part : parts) {
                group.add(new DefaultMutableTreeNode(new CheckBoxNode(part.getDesignator() + " - " + part.getComment(), false, part)));
                System.out.println(part.getDesignator() + " - " + part.getComment());
            }

            root.add(group);
        }

        treeModel.reload();

        revalidate();
        repaint();
    }
}

class CheckBoxNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private PcbPart part;
    private boolean mounted;

    public CheckBoxNode(String text, boolean selected, PcbPart part) {
        this.text = text;
        this.mounted = selected;
        this.part = part;
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
    private JCheckBox checkBox = new JCheckBox();

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof CheckBoxNode) {
            CheckBoxNode checkBoxNode = (CheckBoxNode) userObject;

            checkBox.setText(checkBoxNode.getText());
            checkBox.setSelected(checkBoxNode.isMounted());
            return checkBox;
        } else {
            return new DefaultTreeCellRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }
}
