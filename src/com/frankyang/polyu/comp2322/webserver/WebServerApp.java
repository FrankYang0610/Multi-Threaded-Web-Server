package com.frankyang.polyu.comp2322.webserver;

import com.frankyang.polyu.comp2322.webserver.clientcontroller.ClientController;
import com.frankyang.polyu.comp2322.webserver.config.Config;
import com.frankyang.polyu.comp2322.webserver.servercontroller.ServerController;
import com.frankyang.polyu.comp2322.webserver.gui.MainWindow;

import javax.swing.*;

/**
 * <h2>Multi-Threaded Web Server</h2>
 * <h4>Author: Yang Xikun</h4>
 * <b>Apr, 2025</b>
 *
 * <p>
 *
 * This project implements a web server which is capable of:
 * <ul>
 *   <li>Creating a connection socket when contacted by a client (browser);</li>
 *   <li>Receiving the HTTP request from this connection;</li>
 *   <li>Parsing the request to determine the specific file being requested;</li>
 *   <li>Getting the requested file from the server's file system;</li>
 *   <li>Creating an HTTP response message consisting of the requested file preceded by header lines;</li>
 *   <li>Sending the response over the TCP connection to the requesting client;</li>
 * </ul>
 *
 * <p>
 *
 * This program is the main part of the multi-thread web server project.
 * Please read {@code README.md} before running this program.
 *
 * <p>
 *
 * <b>Notes:</b>
 * <ul>
 *   <li>Please test the server by sending requests from the client programs running on different hosts.</li>
 *   <li>You may run the server on your own computer, using the IP address of {@code 127.0.0.1}.</li>
 *   <li>If you run your server on a host that already has a Web server running on it,
 *       then you should use a different port than port {@code 80} for your Web server.</li>
 *   <li>The project is implemented using the Java Programming Language with basic socket programming classes.</li>
 *   <li>The project is implemented without using the {@code HTTPServer} class directly.</li>
 * </ul>
 *
 * &nbsp;<br>
 *
 * <h3>The {@code WebServerApp} class - the entrance of the entire program</h3>
 * Please run this class to start the entire Web Server.
 */
public class WebServerApp {

    private ServerController serverController;
    private ClientController clientController;

    /**
     * Note that this program uses this {@code WebServerApp} class for the singleton controller.
     * @param args the arguments provided by the caller of the program (will always be ignored).
     */
    public static void main(String[] args) {
        // Start the program
        WebServerApp app = new WebServerApp();
        app.serverController = new ServerController();
        app.clientController = new ClientController(
                Config.SERVER_IP,
                app.serverController.getServerPort()
        );

        // Start the demonstration window first
        // Add the listener immediately to avoid missing the initial messages from the server.
        MainWindow mainWindow = new MainWindow(app.serverController, app.clientController);
        app.serverController.getLogger().addLogListener(mainWindow);

        // Start EDT thread
        SwingUtilities.invokeLater(() -> {
            mainWindow.setVisible(true);
        });

        // Start the server
        app.serverController.boot();
    }
}
