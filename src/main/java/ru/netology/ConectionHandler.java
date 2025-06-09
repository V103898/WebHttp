package ru.netology;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final List<String> validPaths;

    public ConnectionHandler(Socket socket, List<String> validPaths) {
        this.socket = socket;
        this.validPaths = validPaths;
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            Request request;
            try {
                request = Request.fromRequestLine(requestLine);
            } catch (IllegalArgumentException e) {
                sendResponse(out, "400 Bad Request", "text/plain", "Invalid request".getBytes());
                return;
            }

            String path = request.getPath();
            if (!validPaths.contains(path)) {
                sendResponse(out, "404 Not Found", "text/plain", "Not Found".getBytes());
                return;
            }

            Path filePath = Path.of(".", "public", path);
            if (!Files.exists(filePath)) {
                sendResponse(out, "404 Not Found", "text/plain", "File not found".getBytes());
                return;
            }

            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            if (path.equals("/classic.html")) {
                handleClassicHtml(filePath, mimeType, out);
            } else {
                handleRegularFile(filePath, mimeType, out);
            }
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    private void handleClassicHtml(Path filePath, String mimeType, BufferedOutputStream out) throws IOException {
        String template = Files.readString(filePath);
        byte[] content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        sendResponse(out, "200 OK", mimeType, content);
    }

    private void handleRegularFile(Path filePath, String mimeType, BufferedOutputStream out) throws IOException {
        byte[] content = Files.readAllBytes(filePath);
        sendResponse(out, "200 OK", mimeType, content);
    }

    private void sendResponse(BufferedOutputStream out, String status, String contentType, byte[] content) throws IOException {
        String headers = "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(headers.getBytes());
        out.write(content);
        out.flush();
    }
}