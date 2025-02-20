package org.jassistpcb.gui;

import org.jassistpcb.gui.support.Icons;
import org.jassistpcb.utils.CsvImporter;
import org.jassistpcb.utils.PcbPart;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartsPanel extends JPanel {

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<PcbPart> data;

    public PartsPanel() {
        createUI();
    }

    public void createUI() {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Packages Panel",
                TitledBorder.LEFT,
                TitledBorder.TOP
        );
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        setBorder(titledBorder);
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton importPackagesButton = new JButton(importPackagesAction);
        toolBar.add(importPackagesButton);

        JButton importImage = new JButton(importImageAction);
        toolBar.add(importImage);

        toolBar.addSeparator();

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(100, searchField.getPreferredSize().height));
        searchField.setMaximumSize(searchField.getPreferredSize());
        toolBar.add(new JLabel("Search:  "));
        toolBar.add(searchField);

        add(toolBar, BorderLayout.NORTH);

        String[] columnNames = {"Designator", "Comment", "Layer", "Footprint", "Center-X(mm)", "Center-Y(mm)", "Rotation", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(scrollPane);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(5);

        add(splitPane, BorderLayout.CENTER);

        data = new ArrayList<>();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }

            private void filterTable() {
                String searchText = searchField.getText().toLowerCase();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
            }
        });

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    PcbPart selectedPart = data.get(modelRow);
                    System.out.println("Designator do part selecionado: " + selectedPart.getDesignator());
                }
            }
        });
    }

    public final Action importPackagesAction = new AbstractAction() {
        {
            putValue(Action.SMALL_ICON, Icons.addPackage);
            putValue(Action.SHORT_DESCRIPTION, "Importar um arquivo de pick and place (CSV)");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            importPackages();
        }
    };

    public final Action importImageAction = new AbstractAction() {
        {
            putValue(Action.SMALL_ICON, Icons.addImage);
            putValue(Action.SHORT_DESCRIPTION, "Importar um arquivo de imagem");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Selecione o arquivo PNG para importar");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int userSelection = fileChooser.showOpenDialog(PartsPanel.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                MainWindow.getDisplayPanel().displayImage(selectedFile.getAbsolutePath());
            }
        }
    };

    private List<PcbPart> importPackages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione o arquivo CSV para importar");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                List<PcbPart> parts = CsvImporter.processCsv(selectedFile.getPath(), 15);

                data.addAll(parts);

                for(PcbPart part : parts) {
                    System.out.println(part.getDesignator());
                    tableModel.addRow(part.toStringArray());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            MainWindow.getGroupsPanel().updateGroups(data);
            System.out.println("Arquivo selecionado: " + selectedFile.getAbsolutePath());
        }

        return null;
    }

    public void groupAndPrintPartsByComment() {
        Map<String, List<PcbPart>> groupedParts = new HashMap<>();

        for (PcbPart part : data) {
            String comment = part.getComment();  // Supomos que existe o método getComment() em PcbPart
            groupedParts.computeIfAbsent(comment, k -> new ArrayList<>()).add(part);
        }

        for (Map.Entry<String, List<PcbPart>> entry : groupedParts.entrySet()) {
            String comment = entry.getKey();
            List<PcbPart> parts = entry.getValue();

            System.out.println("Grupo de peças com comment: " + comment);
            for (PcbPart part : parts) {
                System.out.println(part.getDesignator() + " - " + part.getComment());
            }
            System.out.println();
        }
    }
}