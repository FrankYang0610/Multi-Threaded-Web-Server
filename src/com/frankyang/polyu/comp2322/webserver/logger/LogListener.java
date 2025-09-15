package com.frankyang.polyu.comp2322.webserver.logger;

/**
 * <h3>The {@code LogListener} interface</h3>
 * This interface is a listener interface. When the {@code Logger} receives a new log, it broadcasts to all {@code LogListener} objects, allowing them to obtain the latest log entry.
 */
public interface LogListener {
    void onLogUpdated(String latestLog);
}
