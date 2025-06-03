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

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            String path = parts[1];
            if (!validPaths.contains(path)) {
                sendResponse(out, "404 Not Found", "text/plain", new byte[0]);
                return;
            }

            Path filePath = Path.of(".", "public", path);
            String mimeType = Files.probeContentType(filePath);

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
