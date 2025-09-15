package com.frankyang.polyu.comp2322.webserver.clientcontroller;

import com.frankyang.polyu.comp2322.webserver.config.Config;
import com.frankyang.polyu.comp2322.webserver.gui.ResponseDialogManager;
import com.frankyang.polyu.comp2322.webserver.util.DateTimeManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <h3>The {@code ClientController} class</h3>
 * This class is the Controller for the Clients Simulator, responsible for sending requests to the Server.
 */
public class ClientController {
    private final String serverIP;
    private final int serverPort;
    private final ResponseDialogManager responseDialogManager = new ResponseDialogManager();

    public ClientController(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void sendGetRequest(String path, boolean keepAlive, Date ifModifiedSince) throws IOException {
        if (keepAlive) {
            // To test keep-alive connection, the client will send the same request to the server twice.
            try (Socket socket = new Socket(serverIP, serverPort)) {
                sendRequest("GET", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "GET", path);

                try {
                    Thread.sleep(1000); // 1s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                sendRequest("GET", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "GET", path);
            }
        } else {
            try (Socket socket = new Socket(serverIP, serverPort)) {
                sendRequest("GET", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "GET", path);
            }
        }
    }

    public void sendHeadRequest(String path, boolean keepAlive, Date ifModifiedSince) throws IOException {
        if (keepAlive) {
            // To test keep-alive connection, the client will send the same request to the server twice.
            try (Socket socket = new Socket(serverIP, serverPort)) {
                sendRequest("HEAD", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "HEAD", path);

                try {
                    Thread.sleep(1000); // 1s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                sendRequest("HEAD", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "HEAD", path);
            }
        } else {
            try (Socket socket = new Socket(serverIP, serverPort)) {
                sendRequest("HEAD", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "HEAD", path);
            }
        }
    }

    public void sendWrongRequest(String path, boolean keepAlive, Date ifModifiedSince) throws IOException {
        if (keepAlive) {
            // To test keep-alive connection, the client will send the same request to the server twice.
            try (Socket socket = new Socket(serverIP, serverPort)) {
                sendRequest("WRONG", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "WRONG", path);

                try {
                    Thread.sleep(1000); // 1s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                sendRequest("WRONG", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "WRONG", path);
            }
        } else {
            try (Socket socket = new Socket(serverIP, serverPort)) {
                sendRequest("WRONG", path, keepAlive, ifModifiedSince, socket);
                processResponse(socket, "WRONG", path);
            }
        }
    }

    private void sendRequest(String method, String path, boolean keepAlive, Date ifModifiedSince, Socket socket)
            throws IOException {
        String encodedPath = encodePath(path);
        String connectionType = keepAlive ? "keep-alive" : "close";

        OutputStream out = socket.getOutputStream();

        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(method).append(" ").append(encodedPath).append(" HTTP/1.1\r\n");
        requestBuilder.append("Host: ").append(serverIP).append(":").append(serverPort).append("\r\n");
        requestBuilder.append("Connection: ").append(connectionType).append("\r\n");
        requestBuilder.append("User-Agent: client\r\n");

        if (ifModifiedSince != null) {
            String formattedDate = DateTimeManager.formatHttpDate(ifModifiedSince.getTime());
            requestBuilder.append("If-Modified-Since: ").append(formattedDate).append("\r\n");
        }

        requestBuilder.append("\r\n");
        String request = requestBuilder.toString();

        out.write(request.getBytes());
        out.flush();
    }

    private void processResponse(Socket socket, String method, String path) throws IOException {
        InputStream in = socket.getInputStream();
        BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));

        List<String> headers = new ArrayList<>();
        String line;
        String contentType = null;
        int contentLength = -1;

        while ((line = headerReader.readLine()) != null && !line.isEmpty()) {
            String trimmedLine = line.trim();
            headers.add(trimmedLine);

            if (trimmedLine.toLowerCase().startsWith("content-type:")) {
                contentType = trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim();
            }

            if (trimmedLine.toLowerCase().startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim());
                } catch (NumberFormatException e) {
                    contentLength = -1;
                }
            }
        }

        if (method.equals("HEAD") || contentLength == 0) {
            responseDialogManager.showTextResponseDialog(headers, "", method + " " + path);
            return;
        }

        if (contentType != null && contentType.startsWith("image/")) {
            processImageResponse(in, headers, method + " " + path, contentLength);
        } else {
            processTextResponse(in, headers, method + " " + path, contentLength);
        }
    }


    private void processTextResponse(InputStream in, List<String> headers, String title, int contentLength) throws IOException {
        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[Config.BUFFER_SIZE];

        if (contentLength > 0) {
            int alreadyRead = 0;
            while (alreadyRead < contentLength) {
                int bytesToRead = Math.min(buffer.length, contentLength - alreadyRead);
                int bytesRead = in.read(buffer, 0, bytesToRead);
                if (bytesRead == -1) { break; }
                bodyBuffer.write(buffer, 0, bytesRead);
                alreadyRead += bytesRead;
            }
        }

        String body = bodyBuffer.toString(StandardCharsets.UTF_8);
        responseDialogManager.showTextResponseDialog(headers, body, title);
    }

    private void processImageResponse(InputStream in, List<String> headers, String title, int contentLength) throws IOException {
        // The buffer to read the image data
        ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[Config.BUFFER_SIZE];

        if (contentLength > 0) {
            int alreadyRead = 0;
            while (alreadyRead < contentLength) {
                int bytesToRead = Math.min(buffer.length, contentLength - alreadyRead);
                int bytesRead = in.read(buffer, 0, bytesToRead);
                if (bytesRead == -1) { break; }
                imageBuffer.write(buffer, 0, bytesRead);
                alreadyRead += bytesRead;
            }
        }

        byte[] imageData = imageBuffer.toByteArray();

        // Display the image in a dialog
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image != null) {
                responseDialogManager.showImageResponseDialog(headers, image, title);
            } else {
                // Failed to parse as image
                responseDialogManager.showErrorDialog(
                        "Error",
                        "Error: Cannot display image. Invalid or unsupported image format."
                );
            }
        } catch (IOException e) {
            responseDialogManager.showErrorDialog(
                    "Error",
                    "Error displaying image: " + e.getMessage());
        }
    }

    private String encodePath(String path) {
        return path.replaceAll(" ", "%20");
    }
}
