package com.frankyang.polyu.comp2322.webserver.servercontroller;

import com.frankyang.polyu.comp2322.webserver.config.Config;
import com.frankyang.polyu.comp2322.webserver.http.HTTPRequestHandler;
import com.frankyang.polyu.comp2322.webserver.threadpool.ServerThreadPool;
import com.frankyang.polyu.comp2322.webserver.logger.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * <h3>The {@code ServerController} class</h3>
 * This class is the Controller of the Server, responsible for controlling the connection and disconnection of the Server socket.
 */
public class ServerController {
    private volatile boolean running;

    private ServerSocket serverSocket;
    private ServerThreadPool threadPool;
    private int serverPort;

    private final Logger logger;

    public ServerController() {
        this.serverPort = Config.DEFAULT_SERVER_PORT;

        if (!isPortAvailable(serverPort)) {
            int newPort = findAvailablePort();
            if (newPort > 0) {
                serverPort = newPort;
            } else {
                System.out.println("Error. No available port is found.");
                System.exit(-1);
            }
        }

        logger = new Logger();
    }

    public int getServerPort() {
        return serverPort;
    }

    /**
     * Check if a port is available.
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Find an available port. Start searching from {@code 8080}.
     */
    private int findAvailablePort() {
        int port = Config.DEFAULT_SERVER_PORT;
        while (port < 65535) {
            if (isPortAvailable(port)) {
                return port;
            }
            port++;
        }
        return -1;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Boot the Web Server.
     */
    public void boot() {
        try {
            running = true;

            logger.log("Welcome", "Welcome to the Multi-Threaded Web Server by YANG Xikun (Project Red-Crowned Crane)");

            serverSocket = new ServerSocket(
                    serverPort,
                    Config.BACKLOG,
                    InetAddress.getByName(Config.SERVER_IP)
            );
            logger.log("", "Server started on port " + serverPort + ".");

            threadPool = new ServerThreadPool(Config.MAX_CONNECTIONS);
            logger.log("", "The thread pool is successfully started.");

            logger.log("", "The server is now listening.");

            while (running) {
                Socket client = serverSocket.accept();
                threadPool.execute(new HTTPRequestHandler(client, Config.WEB_ROOT, logger));
            }
        } catch (IOException e) {
            if (running) {
                logger.log("Java Error: IOException", e.getMessage());
            }
        }
    }

    /**
     * Shutdown the Web Server.
     * <p>
     * This step involves closing the socket and releasing other resources.
     */
    public void shutdown() {
        running = false;

        try { serverSocket.close(); } catch (Exception ignored) { }

        threadPool.shutdown();

        try {
            if (!threadPool.awaitTermination(Config.MAX_AWAIT_TIME, TimeUnit.SECONDS)) {
                threadPool.shutdown();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }

        logger.log("",
                       """
                       The server is shut down. \
                       Please restart the program if you need to restart the server.
                       Bye.
                       """
        );

        logger.removeAllLogListeners();
    }
}
