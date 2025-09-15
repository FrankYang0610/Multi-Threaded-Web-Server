package com.frankyang.polyu.comp2322.webserver.config;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <h3>The {@code Config} class</h3>
 * This class defines the constants assumed within the program, for example, the default server port and the default timeout duration.
 */
public class Config {
    /**
     * Server IP address
     */
    public static final String SERVER_IP = "127.0.0.1";

    /**
     * Server port
     */
    public static final int DEFAULT_SERVER_PORT = 8080;

    // Client IP and Client Port will be assigned automatically by the OS/JVM.
    // See `ClientController.java`

    /**
     * Maximum number of pending connections in the server socket queue
     */
    public static final int BACKLOG = 50;

    /**
     * Maximum number of connections in the Server Thread Pool
     */
    public static final int MAX_CONNECTIONS = 50;

    /**
     * Maximum wait time for incomplete tasks when the user requests to shut down the server
     */
    public static final int MAX_AWAIT_TIME = 5;

    /**
     * Designed to accommodate {@code .jar} file.
     */
    private static String getWebRootPath() {
        String path = "";
        try {
            path = Paths.get(
                    Config.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent().toString();
            String rootPath = Paths.get(path, "resources").toString() + File.separator;
            System.out.println(rootPath);
            return rootPath;
        } catch (URISyntaxException e) {
            System.err.println("URI Syntax Error: " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("Error when loading web root path: " + e.getMessage());
            System.exit(-1);
        }

        String rootPath = "resources" + File.separator;
        System.out.println(rootPath);
        return rootPath;
    }

    /**
     * Root directory exposed by the server
     * <p>
     * Requesting any file not in this directory will result in an HTTP {@code 403 Forbidden} error.
     */
    public static final String WEB_ROOT = getWebRootPath();

    /**
     * Size of the buffer used for reading/writing data (bytes)
     */
    public static final int BUFFER_SIZE = 8192;

    /**
     * List of forbidden files that should not be accessible by the clients
     */
    public static final Set<String> FORBIDDEN_FILES = new HashSet<>(List.of(
            "forbidden.txt"
    ));
}
