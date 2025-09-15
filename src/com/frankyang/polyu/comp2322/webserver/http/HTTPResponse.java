package com.frankyang.polyu.comp2322.webserver.http;

/**
 * <h3>The {@code HTTPResponse} class</h3>
 * This class separates the contents of HTTP responses to facilitate logging.
 */
public class HTTPResponse {
    public final String serverIP;
    public final int serverPort;
    public final String clientIP;
    public final int clientPort;
    public final String datetime;
    public final String method;             // GET or HEAD
    public final String uri;
    public final String httpVersion;
    public final int statusCode;
    public final String reasonPhrase;
    public final long responseBodySize;

    public HTTPResponse(
            String serverIP, int serverPort,
            String clientIP, int clientPort,
            String datetime,
            String method, String uri,
            String httpVersion,
            int statusCode, String reasonPhrase,
            long responseBodySize
    ) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.datetime = datetime;
        this.method = method;
        this.uri = uri;
        this.httpVersion = httpVersion;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.responseBodySize = responseBodySize;
    }

    public String generateLog() {
        return String.format("%s:%d [%s] \"%s %s HTTP/%s\" %d %s (Server: %s:%d) %d bytes",
                clientIP, clientPort, datetime,
                method.toUpperCase(), uri, httpVersion,
                statusCode, reasonPhrase,
                serverIP, serverPort,
                responseBodySize);
    }
}
