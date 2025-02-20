package org.jassistpcb.gui;

import org.jassistpcb.utils.PcbPart;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsPanel extends JPanel {

    private Map<String, List<PcbPart>> groupedParts;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;

    public GroupsPanel() {
        createUI();
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

        root = new DefaultMutableTreeNode("Groups");

        treeModel = new DefaultTreeModel(root);
        JTree tree = new JTree(treeModel);
        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);

        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

            }
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("mouseClicked");
                int x = e.getX();
                int y = e.getY();
                TreePath path = tree.getPathForLocation(x, y);
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object userObject = node.getUserObject();

                    if (userObject instanceof String) {

                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                        int index = parentNode.getIndex(node);


                        if (index < parentNode.getChildCount() - 1) {
                            DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) parentNode.getChildAt(index + 1);

                            parentNode.remove(index);
                            parentNode.insert(node, index + 1);
                            parentNode.insert(nextNode, index);
                            treeModel.reload();
                            tree.repaint();
                        }
                    }

                    if (userObject instanceof CheckBoxNode) {
                        CheckBoxNode checkBoxNode = (CheckBoxNode) userObject;
                        checkBoxNode.setSelected(!checkBoxNode.isSelected());
                        tree.repaint();
                    }
                }
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(tree);
        add(treeScrollPane, BorderLayout.CENTER);
    }

    public void updateGroups(List<PcbPart> data) {
        groupedParts = new HashMap<>();
        root.removeAllChildren();

        for (PcbPart part : data) {
            String comment = part.getComment();
            groupedParts.computeIfAbsent(comment, k -> new ArrayList<>()).add(part);
        }

        for (Map.Entry<String, List<PcbPart>> entry : groupedParts.entrySet()) {
            String comment = entry.getKey();
            List<PcbPart> parts = entry.getValue();

            System.out.println("Grupo de peças com comment: " + comment);

            DefaultMutableTreeNode group = new DefaultMutableTreeNode("Group: " + comment);

            for (PcbPart part : parts) {
                group.add(new DefaultMutableTreeNode(new CheckBoxNode(part.getDesignator() + " - " + part.getComment(), false)));
                System.out.println(part.getDesignator() + " - " + part.getComment());
            }

            root.add(group);
        }

        treeModel.reload();

        revalidate();
        repaint();
    }
}

class CheckBoxNode {
    private String text;
    private boolean selected;

    public CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
            checkBox.setSelected(checkBoxNode.isSelected());
            return checkBox;
        } else {
            // Retorna o renderizador padrão para outros tipos de nós
            return new DefaultTreeCellRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }
}
