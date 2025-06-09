
package ru.netology;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final List<String> validPaths;
    private final ExecutorService threadPool;
    private boolean isRunning;

    public Server(int port, List<String> validPaths) {
        this.port = port;
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(64);
        this.isRunning = false;
    }

    public void start() {
        if (isRunning) {
            throw new IllegalStateException("CEPBEP уже Запущен!");
        }
        isRunning = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("3AnyCK CEPBEPA HA nopTy " + port);

            while (isRunning) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.execute(new ConnectionHandler(socket, validPaths));
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        isRunning = false;
        threadPool.shutdown();
        System.out.println("CEPBEP shutdown");
    }
}
