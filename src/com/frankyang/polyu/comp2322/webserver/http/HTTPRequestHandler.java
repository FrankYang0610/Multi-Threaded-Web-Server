package com.frankyang.polyu.comp2322.webserver.http;

import com.frankyang.polyu.comp2322.webserver.config.Config;
import com.frankyang.polyu.comp2322.webserver.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * <h3>The {@code HTTPRequestHandler} class</h3>
 * This is a multithreaded task model for HTTP requests.
 * Also refer to the {@code HTTPRequestProcessor} class for specific methods of handling HTTP tasks.
 */
public class HTTPRequestHandler implements Runnable {
    private final Socket clientSocket;
    private final String webRoot;
    private final Logger logger;

    public HTTPRequestHandler(Socket clientSocket, String webRoot, Logger logger) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            // set a TIMEOUT limit for keep-alive connections
            // MAX_AWAIT_TIME is in second
            clientSocket.setSoTimeout(Config.MAX_AWAIT_TIME * 1000);

            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            boolean alive = true;
            while (alive) {
                try {
                    HTTPRequestProcessor requestProcessor = new HTTPRequestProcessor(clientSocket, webRoot);
                    HTTPResponse response = requestProcessor.handleRequest(in, out);

                    String logMessage = response.generateLog();
                    if (logMessage != null) {
                        logger.log("", logMessage);
                    }

                    alive = requestProcessor.isKeepAlive();
                } catch (SocketTimeoutException e) {
                    alive = false;
                    logger.log("Warning", "Keep-alive connection timed out.");
                } catch (IOException e) {
                    alive = false;
                    logger.log("Java Error: IOException", e.getMessage());
                }
            }

            in.close();
            out.close();
        } catch (IOException e) {
            logger.log("Java Error: IOException", e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}
