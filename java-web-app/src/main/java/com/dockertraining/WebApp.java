package com.dockertraining;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

public class WebApp {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> sendResponse(exchange, homePage()));
        server.createContext("/health", exchange -> sendResponse(exchange, healthPage()));
        server.createContext("/api/info", exchange -> sendResponse(exchange, apiInfo()));

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:" + port);
    }

    private static void sendResponse(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] bytes = html.getBytes();
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String homePage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Docker Training - Java App</title>
                <style>
                    body { font-family: Arial, sans-serif; background: #1a1a2e; color: #eee;
                           display: flex; justify-content: center; align-items: center;
                           min-height: 100vh; margin: 0; }
                    .card { background: #16213e; border-radius: 16px; padding: 40px 60px;
                            box-shadow: 0 8px 32px rgba(0,0,0,.3); text-align: center; }
                    h1 { color: #0fb9b1; font-size: 2.5em; margin-bottom: 10px; }
                    p  { font-size: 1.2em; color: #a4b0be; }
                    a  { color: #0fb9b1; text-decoration: none; }
                    .badge { background: #0fb9b1; color: #1a1a2e; padding: 6px 16px;
                             border-radius: 20px; font-weight: bold; display: inline-block;
                             margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <h1>🐳 Docker Training</h1>
                    <p>Java Web Application running inside a Docker container!</p>
                    <p>Built by <strong>shashank</strong></p>
                    <br>
                    <span class="badge">✅ Container is running</span>
                    <br><br>
                    <p><a href="/health">Health Check</a> | <a href="/api/info">API Info</a></p>
                </div>
            </body>
            </html>
            """;
    }

    private static String healthPage() {
        return """
            {"status": "UP", "timestamp": "%s"}
            """.formatted(LocalDateTime.now());
    }

    private static String apiInfo() {
        return """
            {"app": "Docker Training Java App", "version": "1.0.0",
             "java": "%s", "os": "%s", "arch": "%s"}
            """.formatted(
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"));
    }
}
