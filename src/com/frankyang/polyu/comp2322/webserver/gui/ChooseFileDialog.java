package com.frankyang.polyu.comp2322.webserver.gui;

import com.frankyang.polyu.comp2322.webserver.clientcontroller.ClientController;
import com.frankyang.polyu.comp2322.webserver.util.DateTimeManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * <h3>The {@code ChooseFileDialog} class</h3>
 * This class implements the GUI window for the Clients Simulator to input the file requested to the server, select the connection type, and enable or disable If-Modified-Since.
 */
public class ChooseFileDialog extends JDialog {
    private final ClientController clientController;

    private final JTextField filepathField;

    /**
     * @param owner the owner of this dialog
     * @param method {@code GET} or {@code HEAD}
     */
    public ChooseFileDialog(Frame owner, String method, ClientController clientController) {
        super(owner, "Client: Select File for HTTP " + method, true);
        this.clientController = clientController;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Set the window layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // The information label
        JPanel infoPanel = getInfoPanel();

        // Filepath textfield and browse button
        JPanel filepathPanel = new JPanel(new BorderLayout(10, 0));
        filepathPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        filepathField = new JTextField(30);
        filepathField.setText("/text.txt");
        filepathField.selectAll();
        filepathField.requestFocusInWindow();
        JButton browseButton = new JButton("Browse");
        browseButton.setEnabled(false);
        filepathPanel.add(filepathField, BorderLayout.CENTER);
        filepathPanel.add(browseButton, BorderLayout.EAST);

        // The button panel and the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JButton cancelButton = new JButton("Cancel");
        JButton submitButton = new JButton("Submit");

        // The connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection Type"));
        ButtonGroup connectionGroup = new ButtonGroup();
        JRadioButton closeConnectionRadioButton = new JRadioButton("close");
        JRadioButton keepAliveRadioButton = new JRadioButton("keep-alive");
        closeConnectionRadioButton.setSelected(true);
        connectionGroup.add(closeConnectionRadioButton);
        connectionGroup.add(keepAliveRadioButton);
        connectionPanel.add(closeConnectionRadioButton);
        connectionPanel.add(keepAliveRadioButton);

        // The if-modified-since panel
        JPanel ifModifiedSincePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ifModifiedSincePanel.setBorder(BorderFactory.createTitledBorder("Test If-Modified-Since feature (Local time)"));
        JCheckBox ifModifiedSinceCheckBox = new JCheckBox("Enable If-Modified-Since:");
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateTimeSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(
                dateTimeSpinner,
                DateTimeManager.NO_TIME_ZONE_PATTERN
        );
        dateEditor.getFormat().setTimeZone(TimeZone.getDefault());
        dateTimeSpinner.setEditor(dateEditor);
        dateTimeSpinner.setEnabled(false);
        dateTimeSpinner.revalidate();
        dateTimeSpinner.repaint();
        ifModifiedSincePanel.add(ifModifiedSinceCheckBox);
        ifModifiedSincePanel.add(dateTimeSpinner);

        // Button listeners
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(ChooseFileDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                filepathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
            filepathField.selectAll();
            filepathField.requestFocusInWindow();
        });
        cancelButton.addActionListener(e -> dispose());
        submitButton.addActionListener(e -> {
            String filepath = filepathField.getText().trim();
            if (filepath.isEmpty()) {
                JOptionPane.showMessageDialog(
                        ChooseFileDialog.this,
                        "Please select a file!",
                        "Empty filepath error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean keepAlive = keepAliveRadioButton.isSelected();

            Date ifModifiedSinceDate = null;
            if (ifModifiedSinceCheckBox.isSelected()) {
                ifModifiedSinceDate = (Date) dateTimeSpinner.getValue();
            }

            try {
                switch (method) {
                    case "GET" ->
                            this.clientController.sendGetRequest(filepath, keepAlive, ifModifiedSinceDate); // ignore the return value
                    case "HEAD" ->
                            this.clientController.sendHeadRequest(filepath, keepAlive, ifModifiedSinceDate); // ignore the return value
                    case "WRONG" ->
                            this.clientController.sendWrongRequest(filepath, keepAlive, ifModifiedSinceDate); // ignore the return value
                    default -> {
                        JOptionPane.showMessageDialog(
                                ChooseFileDialog.this,
                                "This program currently only supports HTTP GET and HTTP HEAD.",
                                "Unsupported HTTP Request Type",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                }
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(
                        ChooseFileDialog.this,
                        "Cannot send the HTTP request: " + exception.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            dispose();
        });

        // Add other listener(s)
        ifModifiedSinceCheckBox.addActionListener(e ->
                dateTimeSpinner.setEnabled(ifModifiedSinceCheckBox.isSelected())
        );

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        // buttonPanel.add(Box.createHorizontalStrut(0));
        buttonPanel.add(submitButton);

        mainPanel.add(infoPanel);
        mainPanel.add(filepathPanel);
        mainPanel.add(connectionPanel);
        mainPanel.add(ifModifiedSincePanel);
        mainPanel.add(buttonPanel);

        setContentPane(mainPanel);
        getRootPane().setDefaultButton(submitButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        // ESC button logic
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel getInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel infoLabel = new JLabel(
                "<html>The Server and Client should be separate.<p>" +
                        "Therefore, you cannot browse files here and must manually enter the file path.<p><p>" +
                        "If you choose keep-alive, to better illustrate the essence of keep-alive,<p>" +
                        "the client will send the same request twice to the server.</html>"
        );
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(infoLabel);
        return infoPanel;
    }
}
