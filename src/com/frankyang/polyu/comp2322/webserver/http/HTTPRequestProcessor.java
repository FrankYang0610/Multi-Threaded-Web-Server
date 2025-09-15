package com.frankyang.polyu.comp2322.webserver.http;

import com.frankyang.polyu.comp2322.webserver.config.Config;
import com.frankyang.polyu.comp2322.webserver.util.DateTimeManager;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3>The {@code HTTPRequestProcessor} class</h3>
 * This class receives HTTP requests from the client, processes them, and then returns the corresponding response to the clients.
 */
public class HTTPRequestProcessor {
    private final Socket clientSocket;
    private final String webRoot;
    private String method;
    private String uri;
    private String httpVersion = "1.1";
    private boolean keepAlive = false;

    /**
     * Supported media types
     */
    private static final Map<String, String> SUPPORTED_TYPES = Map.ofEntries(
            Map.entry(".txt", "text/plain"),
            Map.entry(".md", "text/markdown"),
            Map.entry(".c", "text/x-c"),
            Map.entry(".cpp", "text/x-c++"),
            Map.entry(".h", "text/x-c"),
            Map.entry(".hpp", "text/x-c++"),
            Map.entry(".java", "text/x-java-source"),
            Map.entry(".py", "text/x-python"),
            Map.entry(".js", "application/javascript"),
            Map.entry(".ts", "application/typescript"),
            Map.entry(".html", "text/html"),
            Map.entry(".htm", "text/html"),
            Map.entry(".css", "text/css"),
            Map.entry(".swift", "text/x-swift"),
            Map.entry(".kt", "text/x-kotlin"),
            Map.entry(".jpg", "image/jpeg"),
            Map.entry(".jpeg", "image/jpeg"),
            Map.entry(".png", "image/png")
    );

    public HTTPRequestProcessor(Socket clientSocket, String webRoot) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
    }

    /**
     *
     * @param in Input stream
     * @param out Output stream
     * @return Information about the HTTP request result, which is the information that needs to be logged.
     * @throws IOException
     */
    public HTTPResponse handleRequest(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String requestLine = reader.readLine();
        String datetime = DateTimeManager.getDateTime();

        // Parse the request line
        boolean isValidRequestLine = requestLine != null && parseRequestLine(requestLine);

        // Read the request head
        Map<String, String> requestHeaders = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int delimiterIndex = line.indexOf(":");
            if (delimiterIndex > 0) {
                requestHeaders.put(
                        line.substring(0, delimiterIndex).trim(),
                        line.substring(delimiterIndex + 1).trim()
                );
            }
        }

        // Check and update the connection type
        String connectionHeader = requestHeaders.getOrDefault("Connection", "");
        keepAlive = "keep-alive".equalsIgnoreCase(connectionHeader) && httpVersion.equals("1.1");

        // Handle the empty line
        // I don't know why I have to write this,
        // but once this part is added, the program works.
        if (requestLine == null ||
                requestLine.isEmpty()
        ) {
            return null;
        }

        // Add additional support for OPTION (for clients in browser)
        if (isValidRequestLine && method.equals("OPTIONS")) {
            sendOptionsResponse(out);
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 200, "OK", 0
            );
        }

        // Judge the request line
        if (!isValidRequestLine ||
                (!method.equals("GET") && !method.equals("HEAD"))
        ) {
            sendError(out, 400, "Bad Request");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, "-", "-", "-", 400, "Bad Request", 0
            );
        }

        // Process special characters.
        // e.g., decode `my%20file.txt` to `my file.txt`.
        String decodedURI;
        try {
            decodedURI = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            sendError(out, 400, "Bad Request");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 400, "Bad Request", 0
            );
        }

        // Access to the forbidden file is not allowed
        String relativePath = decodedURI.startsWith("/") ? decodedURI.substring(1) : decodedURI;
        if (Config.FORBIDDEN_FILES.contains(relativePath)) {
            sendError(out, 403, "Forbidden");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 403, "Forbidden", 0
            );
        }

        // Find the requested file
        File file = new File(webRoot, decodedURI);

        // Prevent Directory Traversal Attacks
        // Use the canonical mechanism to get a unique representation of the path and prevent directory traversal.
        // The client can only access the folders under `resources/`.
        File canonicalWebRoot = new File(webRoot).getCanonicalFile();
        File canonicalFile = file.getCanonicalFile();
        if (!canonicalFile.getPath().startsWith(canonicalWebRoot.getPath())) {
            sendError(out, 403, "Forbidden");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 403, "Forbidden", 0
            );
        }

        // Check if the file exists
        if (!canonicalFile.exists() || !canonicalFile.isFile()) {
            sendError(out, 404, "File Not Found");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 404, "Not Found", 0
            );
        }

        // Check if the file can read
        if (!canonicalFile.canRead()) {
            sendError(out, 403, "Forbidden");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 403, "Forbidden", 0
            );
        }

        // Check the type of the file
        String fileExtension = decodedURI.contains(".")
                ? decodedURI.substring(decodedURI.lastIndexOf(".")).toLowerCase()
                : "";
        String contentType = SUPPORTED_TYPES.get(fileExtension);
        if (contentType == null) {
            sendError(out, 415, "Unsupported Media Type");
            return new HTTPResponse(
                    clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                    datetime, method, uri, httpVersion, 415, "Unsupported Media Type", 0
            );
        }

        // Check if the file has not been modified.
        long lastModified = canonicalFile.lastModified();
        String lastModifiedStr = DateTimeManager.formatHttpDate(lastModified);
        String ifModifiedSinceStr = requestHeaders.getOrDefault("If-Modified-Since", null);

        if (ifModifiedSinceStr != null) {
            long ifModifiedSince = DateTimeManager.parseHttpDate(ifModifiedSinceStr);

            // Java uses millisecond precision, but the RFC specifies second-level precision.
            long lastModifiedRFC = lastModified / 1000;
            long ifModifiedSinceRFC = ifModifiedSince / 1000;

            if (lastModifiedRFC <= ifModifiedSinceRFC) {
                sendNotModified(out, lastModifiedStr);
                return new HTTPResponse(
                        clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                        datetime, method, uri, httpVersion, 304, "Not Modified", 0
                );
            }
        }

        if (method.equals("GET")) {
            sendOK(out, contentType, canonicalFile);
        } else if (method.equals("HEAD")) {
            sendHead(out, contentType, canonicalFile.length(), lastModifiedStr);
        }

        return new HTTPResponse(
                clientSocket.getLocalAddress().getHostAddress(), clientSocket.getLocalPort(),
                clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),
                datetime, method, uri, httpVersion, 200, "OK", canonicalFile.length()
        );
    }

    private boolean parseRequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");

        if (tokens.length < 3) {
            return false;
        }

        method = tokens[0];
        uri = tokens[1];
        httpVersion = tokens[2].substring(tokens[2].indexOf("/") + 1);

        return true;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    private void sendOptionsResponse(OutputStream out) throws IOException {
        String header = "HTTP/" + httpVersion + " 200 OK\r\n" +
                "Date: " + DateTimeManager.formatHttpDate(System.currentTimeMillis()) + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Connection, If-Modified-Since\r\n" +
                "Access-Control-Max-Age: 86400\r\n" +  // Cache the result of OPTIONS request for one day
                "Content-Length: 0\r\n" +
                "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();
    }

    private void sendOK(OutputStream out, String contentType, File file) throws IOException {
        String header = "HTTP/" + httpVersion + " 200 OK\r\n" +
                "Date: " + DateTimeManager.formatHttpDate(System.currentTimeMillis()) + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + file.length() + "\r\n" +
                "Last-Modified: " + DateTimeManager.formatHttpDate(file.lastModified()) + "\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Expires: 0\r\n" +
                "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Connection, If-Modified-Since\r\n" +
                "Access-Control-Expose-Headers: *\r\n" +
                "\r\n";
        out.write(header.getBytes());

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[Config.BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        out.flush();
    }

    private void sendHead(OutputStream out, String contentType, long contentLength, String lastModifiedStr)
            throws IOException {
        String header = "HTTP/" + httpVersion + " 200 OK\r\n" +
                "Date: " + DateTimeManager.formatHttpDate(System.currentTimeMillis()) + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Expires: 0\r\n" +
                "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Connection, If-Modified-Since\r\n" +
                "Access-Control-Expose-Headers: *\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();
    }

    private void sendNotModified(OutputStream out, String lastModifiedStr) throws IOException {
        String header = "HTTP/" + httpVersion + " 304 Not Modified\r\n" +
                "Date: " + DateTimeManager.formatHttpDate(System.currentTimeMillis()) + "\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Expires: 0\r\n" +
                "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Connection, If-Modified-Since\r\n" +
                "Access-Control-Expose-Headers: *\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();
    }

    private void sendError(OutputStream out, int statusCode, String reasonPhrase) throws IOException {
        String body = "<html><body>Error: " + statusCode + " " + reasonPhrase + "</body></html>";
        String header = "HTTP/" + httpVersion + " " + statusCode + " " + reasonPhrase + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Expires: 0\r\n" +
                "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Connection, If-Modified-Since\r\n" +
                "Access-Control-Expose-Headers: *\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.write(body.getBytes());
        out.flush();
    }
}
