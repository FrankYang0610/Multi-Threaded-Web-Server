package com.frankyang.polyu.comp2322.webserver.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * <h3>The {@code ResponseDialogManager} class</h3>
 * This is the collection of windows in the Clients side that receives responses from the Server.
 */
public class ResponseDialogManager {
    public void showTextResponseDialog(List<String> headers, String body, String title) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog();

            dialog.setTitle("Client: HTTP Response: " + title);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            StringBuilder content = new StringBuilder();

            // Add headers
            for (String header : headers) {
                content.append(header).append("\r\n");
            }

            // Add body
            content.append(body).append("\n");

            textArea.setText(content.toString());

            JScrollPane scrollPane = new JScrollPane(textArea);
            dialog.add(scrollPane);

            JPanel buttonPanel = new JPanel();
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.getRootPane().setDefaultButton(closeButton);
            dialog.setVisible(true);
        });
    }

    public void showImageResponseDialog(List<String> headers, BufferedImage image, String title) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Client: HTTP Response: " + title);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel mainPanel = new JPanel(new BorderLayout());

            // Headers panel
            JTextArea headersArea = new JTextArea();
            headersArea.setEditable(false);
            headersArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

            StringBuilder headersText = new StringBuilder();
            for (String header : headers) {
                headersText.append(header).append("\r\n");
            }
            headersArea.setText(headersText.toString());

            JScrollPane headersScrollPane = new JScrollPane(headersArea);
            headersScrollPane.setPreferredSize(new Dimension(800, 150));

            // Image panel with scaling
            JPanel imagePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (image != null) {
                        int panelWidth = getWidth(), panelHeight = getHeight();
                        int imgWidth = image.getWidth(), imgHeight = image.getHeight();

                        double scale = Math.min(
                                (double) panelWidth / imgWidth,
                                (double) panelHeight / imgHeight
                        );

                        if (scale > 1.0) { scale = 1.0; }
                        int scaledImgWidth = (int) (imgWidth * scale), scaledImgHeight = (int) (imgHeight * scale);

                        // Center the image
                        int x = (panelWidth - scaledImgWidth) / 2;
                        int y = (panelHeight - scaledImgHeight) / 2;

                        // Render the image
                        Graphics2D g2d = (Graphics2D)g;
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2d.drawImage(image, x, y, scaledImgWidth, scaledImgHeight, null);
                    }
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(800, 400);
                }
            };
            imagePanel.setPreferredSize(new Dimension(800, 450));

            JScrollPane imageScrollPane = new JScrollPane(imagePanel);
            mainPanel.add(headersScrollPane, BorderLayout.NORTH);
            mainPanel.add(imageScrollPane, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel();
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(mainPanel);
            dialog.getRootPane().setDefaultButton(closeButton);
            dialog.setVisible(true);
        });
    }

    public void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
}
