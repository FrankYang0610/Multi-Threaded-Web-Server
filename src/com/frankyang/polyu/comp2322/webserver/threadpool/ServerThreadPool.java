package com.frankyang.polyu.comp2322.webserver.threadpool;

import java.util.concurrent.*;

/**
 * <h3>The {@code ServerThreadPool} class</h3>
 * This class defines the <b>thread pool</b>. The {@code ServerController} class holds the thread pool object.
 * When the socket receives a new request, it will request a new thread from the thread pool to handle the HTTP request and return the response. This class uses {@code ExecutorService} internally.
 */
public class ServerThreadPool {
    private final ExecutorService executor;

    public ServerThreadPool(int numThreads) {
        executor = Executors.newFixedThreadPool(numThreads);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }
}
