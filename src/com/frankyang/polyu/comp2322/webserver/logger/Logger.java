package com.frankyang.polyu.comp2322.webserver.logger;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>The {@code Logger} class</h3>
 * The class to handle web server logs.
 * <p>
 * The logger has a buffer for recording log information
 * and can also be requested by the caller to output log information to a file.
 */
public class Logger {
    private final StringBuffer allLog = new StringBuffer();

    private final List<LogListener> listeners = new ArrayList<>();

    public void addLogListener(LogListener listener) {
        listeners.add(listener);
    }

    public void removeAllLogListeners() {
        listeners.clear();
    }

    private void notifyListeners(String newLog) {
        for (LogListener listener : listeners) {
            listener.onLogUpdated(newLog);
        }
    }

    /**
     * @param title The title of the message, can be {@code ""} (successful) or {@code "ERROR"}.
     * @param rawMessage Raw message from the overloaded version of the {@code log()} method
     */
    public synchronized void log(String title, String rawMessage) {
        if (title == null) {
            title = "";
        }

        if (rawMessage == null || rawMessage.isEmpty()) {
            rawMessage = " [Empty Message] ";
        }

        String latestLog = title.isEmpty() ? rawMessage : ("[" + title + "] " + rawMessage);

        allLog.append(latestLog);
        notifyListeners(latestLog);
    }

    /**
     * @return All logs
     */
    public String getAllLog() {
        return allLog.toString();
    }
}
