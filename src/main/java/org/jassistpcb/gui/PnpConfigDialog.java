package org.jassistpcb.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PnpConfigDialog extends JDialog {

    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JButton openButton;
    private JButton confirmButton;
    private JButton cancelButton;
    private File selectedFile;
    private JLabel textLabel;

    public PnpConfigDialog(JFrame parent) {
        super(parent, "Selecione arquivo CSV para importar", true);
        initializeComponents();
        layoutComponents();
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {

        textLabel = new JLabel("File Preview");

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);

        openButton = new JButton("Abrir Arquivo");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(PnpConfigDialog.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    loadFileContent(selectedFile);
                    confirmButton.setEnabled(true);
                }
            }
        });

        confirmButton = new JButton("Confirmar");
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedFile = null;
                dispose();
            }
        });
    }

    private void layoutComponents() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(openButton);
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);


        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        this.add(textLabel, BorderLayout.NORTH);
        this.add(panel);
    }

    private void loadFileContent(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            textArea.read(reader, null);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retorna o arquivo selecionado pelo usuário.
     * @return O arquivo selecionado, ou null se o usuário cancelou.
     */
    public File getSelectedFile() {
        return selectedFile;
    }
}