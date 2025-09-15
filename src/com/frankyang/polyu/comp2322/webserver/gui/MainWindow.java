package com.frankyang.polyu.comp2322.webserver.gui;

import com.frankyang.polyu.comp2322.webserver.clientcontroller.ClientController;
import com.frankyang.polyu.comp2322.webserver.logger.LogListener;
import com.frankyang.polyu.comp2322.webserver.servercontroller.ServerController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <h3>The {@code MainWindow} class</h3>
 * The Web Server is designed with a GUI interface to let you control clients and view the server's status simultaneously.
 * <p>
 * Theoretically, the server and client should be separate programs.
 * <b>However, for convenience in demonstration, they have been integrated into the same window.
 * Their corresponding controllers remained separate.</b>
 * <p>
 * The window is implemented using Javax Swing.
 */
public class MainWindow extends JFrame implements LogListener {
    private static final String TITLE = "Multi-Threaded Web Server Demonstration";
    private static final String PROJECT_NAME = " (Project Red-Crowned Crane) ";
    private static final String AUTHOR = "Yang Xikun";

    // The size of the client panel
    private static final int CLIENT_PANEL_WIDTH = 350;

    // The size of the buttons (all buttons are within the client panel)
    private static final int BUTTON_WIDTH = CLIENT_PANEL_WIDTH;
    private static final int BUTTON_HEIGHT = 40;
    private static final Dimension BUTTON_SIZE = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);


    private final ServerController serverController;
    private final ClientController clientController;

    private JTextArea logContents;

    private final List<JButton> buttons = new ArrayList<>();

    private boolean running = true;


    public MainWindow(ServerController serverController, ClientController clientController) {
        this.serverController = serverController;
        this.clientController = clientController;

        // Initialize the window
        setTitle(TITLE + PROJECT_NAME);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Set the window layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Render the information area
        JPanel infoPanel = getInfoPanel();

        // The content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Left side: the server area (flexible width)
        JPanel serverPanel = getServerPanel();

        // Right side: the client area (fixed width)
        JPanel clientPanel = getClientPanel();

        // contentPanel - overall control
        contentPanel.add(serverPanel, BorderLayout.CENTER);
        contentPanel.add(clientPanel, BorderLayout.EAST);

        // Window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                shutdownServer();
                dispose();
                System.exit(0);
            }
        });

        // Window - overall control
        mainPanel.add(Box.createVerticalStrut(20)); // spacer
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(20)); // spacer
        mainPanel.add(contentPanel);
        setContentPane(mainPanel);
        setVisible(true);
    }

    private JPanel getInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("<html>" + TITLE + " <span style='font-size:14pt;'>" + PROJECT_NAME + "</span></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(titleLabel);

        JLabel authorLabel = new JLabel("Author: " + AUTHOR);
        authorLabel.setFont(new Font("Arial", Font.BOLD, 16));
        authorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(authorLabel);

        infoPanel.add(Box.createVerticalStrut(10));

        JLabel infoLabel = new JLabel(
                "<html><div style='text-align:center;'>" +
                        "This window allows you to control both the Server side and the Client side at the same time.<br>" +
                        "Note that in accordance with the instruction document, " +
                        "this program <b>only</b> display the log information <b>on the server side</b>." +
                        "</div></html>"
        );
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(infoLabel);

        return infoPanel;
    }

    private JPanel getServerPanel() {
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createTitledBorder("Server Side"));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel serverLogLabel = new JLabel("All server log will be displayed below:");
        serverLogLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton shutdownButton = new JButton("Shutdown Server");
        buttons.add(shutdownButton);
        shutdownButton.setPreferredSize(BUTTON_SIZE);
        shutdownButton.setMaximumSize(BUTTON_SIZE);
        shutdownButton.setMinimumSize(BUTTON_SIZE);
        shutdownButton.setForeground(Color.RED);
        shutdownButton.setFont(new Font("Arial", Font.BOLD, shutdownButton.getFont().getSize()));
        shutdownButton.setFocusPainted(false);
        shutdownButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        topPanel.add(serverLogLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(shutdownButton);

        serverPanel.add(Box.createVerticalStrut(10));
        serverPanel.add(topPanel, BorderLayout.NORTH);
        serverPanel.add(Box.createVerticalStrut(10));

        logContents = new JTextArea();
        logContents.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logContents);
        serverPanel.add(logScrollPane, BorderLayout.CENTER);

        // Button listener
        shutdownButton.addActionListener(e -> shutdownServer());

        return serverPanel;
    }

    private JPanel getClientPanel() {
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Clients Simulator"));
        clientPanel.setLayout(new BoxLayout(clientPanel, BoxLayout.Y_AXIS));

        Dimension clientPanelDimension = new Dimension(CLIENT_PANEL_WIDTH, 0);
        clientPanel.setPreferredSize(clientPanelDimension);
        clientPanel.setMaximumSize(new Dimension(CLIENT_PANEL_WIDTH, Integer.MAX_VALUE));
        clientPanel.setMinimumSize(new Dimension(CLIENT_PANEL_WIDTH, 0));


        // Buttons
        JButton testViaBrowserHeadButton = new JButton("Test via Browser");
        buttons.add(testViaBrowserHeadButton);
        JButton httpGetButton = new JButton("HTTP GET");
        buttons.add(httpGetButton);
        JButton httpHeadButton = new JButton("HTTP HEAD");
        buttons.add(httpHeadButton);
        JButton httpWrongButton = new JButton("HTTP WRONG METHOD");
        buttons.add(httpWrongButton);

        testViaBrowserHeadButton.setPreferredSize(BUTTON_SIZE);
        testViaBrowserHeadButton.setMaximumSize(BUTTON_SIZE);
        testViaBrowserHeadButton.setMinimumSize(BUTTON_SIZE);

        httpGetButton.setPreferredSize(BUTTON_SIZE);
        httpGetButton.setMaximumSize(BUTTON_SIZE);
        httpGetButton.setMinimumSize(BUTTON_SIZE);

        httpHeadButton.setPreferredSize(BUTTON_SIZE);
        httpHeadButton.setMaximumSize(BUTTON_SIZE);
        httpHeadButton.setMinimumSize(BUTTON_SIZE);

        httpWrongButton.setPreferredSize(BUTTON_SIZE);
        httpWrongButton.setMaximumSize(BUTTON_SIZE);
        httpWrongButton.setMinimumSize(BUTTON_SIZE);


        // Button event listeners
        testViaBrowserHeadButton.addActionListener(e -> {
            try {
                String sourcePath = "";
                try {
                    sourcePath = Paths.get(MainWindow.class.getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI())
                            .getParent()
                            .toString() + File.separator;
                } catch (Exception exception) {
                    sourcePath = "";
                    System.err.println("Error determining base path: " + exception.getMessage());
                }

                File htmlFile = new File(
                        Paths.get(sourcePath, "client-website", "client-index.html").toString()
                );

                if (!htmlFile.exists()) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "index.html not found!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                URI uri = htmlFile.toURI();
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Desktop is not supported on this system.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "Failed to open file: " + exception.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        httpGetButton.addActionListener(e -> {
            ChooseFileDialog chooseFileDialog = new ChooseFileDialog(this, "GET", clientController);
            chooseFileDialog.setVisible(true);
        });
        httpHeadButton.addActionListener(e -> {
            ChooseFileDialog chooseFileDialog = new ChooseFileDialog(this, "HEAD", clientController);
            chooseFileDialog.setVisible(true);
        });
        httpWrongButton.addActionListener(e -> {
            ChooseFileDialog chooseFileDialog = new ChooseFileDialog(this, "WRONG", clientController);
            chooseFileDialog.setVisible(true);
        });


        // Text labels
        JLabel multithreadGuide = new JLabel(
                "<html><h3>Each time the client submits an HTTP request, " +
                        "the server will start a new thread to handle it.</h3></html>"
        );
        JLabel testViaBrowserGuide = new JLabel(
                "<html><h3>According to the instruction document's requirements, " +
                        "it's <i>best</i> for the client's request to be sent from the browser:</h3></html>"
        );
        JLabel fileGuide = new JLabel(
                "<html>" +
                        "<h3>You can also choose to test clients here instead of in the browser:</h3>" +
                        "When you click one of the following two buttons, a window will ask you for the requested file path. " +
                        "You can enter it manually or select it from the system files. " +
                        "The server-side log will display the corresponding request results.<p><p>" +
                        "</html>"
        );
        JLabel httpGetGuide = new JLabel(
                "<html>Click this button to test the HTTP GET function. " +
                        "HTTP GET will return the response headers and file content to the client (if applicable). </html>"
        );
        JLabel httpHeadGuide = new JLabel(
                "<html>Click this button to test the HTTP HEAD function. " +
                        "HTTP HEAD only returns the <b>response headers</b> and <b>will not include the file content</b>.</html>"
        );
        JLabel httpWrongGuide = new JLabel(
                "<html>Click this button to trigger HTTP 400 Bad Request.</html>"
        );


        // Separator
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));


        clientPanel.add(Box.createVerticalStrut(20));
        clientPanel.add(multithreadGuide);
        clientPanel.add(Box.createVerticalStrut(30));
        clientPanel.add(testViaBrowserGuide);
        clientPanel.add(testViaBrowserHeadButton);
        clientPanel.add(Box.createVerticalStrut(30));
        clientPanel.add(fileGuide);
        clientPanel.add(Box.createVerticalStrut(10));
        clientPanel.add(httpGetGuide);
        clientPanel.add(httpGetButton);
        clientPanel.add(Box.createVerticalStrut(20));
        clientPanel.add(httpHeadGuide);
        clientPanel.add(httpHeadButton);
        clientPanel.add(Box.createVerticalStrut(20));
        clientPanel.add(httpWrongGuide);
        clientPanel.add(httpWrongButton);

        return clientPanel;
    }

    @Override
    public void onLogUpdated(String latestLog) {
        SwingUtilities.invokeLater(() -> {
            logContents.append(latestLog + "\n");
            logContents.setCaretPosition(logContents.getDocument().getLength());
        });
    };

    private void shutdownServer() {
        if (running) {
            int confirm = JOptionPane.showConfirmDialog(
                    MainWindow.this,
                    "Are you sure to shutdown the server? You must restart this program to reboot the server.",
                    "Server Shutdown",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (confirm == JOptionPane.OK_OPTION) {
                // Firstly, disable all buttons.
                for (JButton button : buttons) {
                    button.setEnabled(false);
                }

                // Shut down the server controller (i.e., shut down the server socket).
                // No need to 'shut down' the client controller.
                if (serverController != null) {
                    serverController.shutdown();
                }

                logContents.setForeground(Color.GRAY);

                running = false;
            }
        }
    }
}
