package com.dockertraining;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebApp {

    private static final String VERSION = "1.0.1";
    private static final long START_TIME = System.currentTimeMillis();

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

    private static void sendResponse(HttpExchange exchange, String body) throws IOException {
        String contentType = body.startsWith("{") ? "application/json" : "text/html";
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String homePage() {
        String uptime = getUptime();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Docker Training - Java App v%s</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        font-family: 'Segoe UI', Arial, sans-serif;
                        background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
                        min-height: 100vh; color: #eee; padding: 30px 20px;
                    }
                    .container { max-width: 900px; margin: 0 auto; }
                    header { text-align: center; padding: 30px 0 40px; }
                    header h1 { font-size: 3em; color: #0fb9b1; letter-spacing: 2px; }
                    header p  { color: #a4b0be; font-size: 1.1em; margin-top: 8px; }
                    .version-badge {
                        display: inline-block; background: #0fb9b1; color: #1a1a2e;
                        padding: 4px 14px; border-radius: 20px; font-size: 0.85em;
                        font-weight: bold; margin-top: 10px;
                    }

                    /* ── Stats row ── */
                    .stats {
                        display: grid; grid-template-columns: repeat(3, 1fr);
                        gap: 20px; margin-bottom: 30px;
                    }
                    .stat-card {
                        background: rgba(255,255,255,0.06); border-radius: 12px;
                        padding: 24px; text-align: center;
                        border: 1px solid rgba(15,185,177,0.2);
                        transition: transform .2s;
                    }
                    .stat-card:hover { transform: translateY(-4px); }
                    .stat-card .icon { font-size: 2em; margin-bottom: 8px; }
                    .stat-card .label { color: #a4b0be; font-size: 0.85em; text-transform: uppercase; letter-spacing: 1px; }
                    .stat-card .value { color: #0fb9b1; font-size: 1.6em; font-weight: bold; margin-top: 4px; }

                    /* ── Info cards ── */
                    .cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 30px; }
                    .card {
                        background: rgba(255,255,255,0.06); border-radius: 12px;
                        padding: 24px; border: 1px solid rgba(255,255,255,0.08);
                    }
                    .card h2 { color: #0fb9b1; font-size: 1em; text-transform: uppercase;
                               letter-spacing: 1px; margin-bottom: 14px; }
                    .card table { width: 100%%; border-collapse: collapse; }
                    .card td { padding: 7px 4px; font-size: 0.92em; }
                    .card td:first-child { color: #a4b0be; width: 45%%; }
                    .card td:last-child  { color: #eee; font-weight: 500; }

                    /* ── Nav links ── */
                    .nav { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
                    .nav a {
                        background: rgba(15,185,177,0.15); color: #0fb9b1;
                        text-decoration: none; padding: 10px 24px; border-radius: 8px;
                        border: 1px solid #0fb9b1; font-weight: 600; transition: background .2s;
                    }
                    .nav a:hover { background: #0fb9b1; color: #1a1a2e; }

                    footer { text-align: center; margin-top: 40px; color: #576574; font-size: 0.85em; }
                </style>
            </head>
            <body>
            <div class="container">
                <header>
                    <h1>🐳 Docker Training</h1>
                    <p>Java Web Application running inside a Docker container</p>
                    <span class="version-badge">v%s</span>
                </header>

                <!-- Stats -->
                <div class="stats">
                    <div class="stat-card">
                        <div class="icon">⚡</div>
                        <div class="label">Status</div>
                        <div class="value">RUNNING</div>
                    </div>
                    <div class="stat-card">
                        <div class="icon">⏱️</div>
                        <div class="label">Uptime</div>
                        <div class="value">%s</div>
                    </div>
                    <div class="stat-card">
                        <div class="icon">☕</div>
                        <div class="label">Java Version</div>
                        <div class="value">%s</div>
                    </div>
                </div>

                <!-- Info cards -->
                <div class="cards">
                    <div class="card">
                        <h2>🖥 System Info</h2>
                        <table>
                            <tr><td>OS</td><td>%s</td></tr>
                            <tr><td>Architecture</td><td>%s</td></tr>
                            <tr><td>Available CPUs</td><td>%d</td></tr>
                            <tr><td>Max Memory</td><td>%d MB</td></tr>
                        </table>
                    </div>
                    <div class="card">
                        <h2>📦 App Info</h2>
                        <table>
                            <tr><td>App Name</td><td>java-web-app</td></tr>
                            <tr><td>Version</td><td>%s</td></tr>
                            <tr><td>Built By</td><td>shashank</td></tr>
                            <tr><td>Server Time</td><td>%s</td></tr>
                        </table>
                    </div>
                </div>

                <!-- Nav -->
                <div class="nav">
                    <a href="/">🏠 Home</a>
                    <a href="/health">❤️ Health Check</a>
                    <a href="/api/info">📡 API Info</a>
                </div>

                <footer>Docker Training &copy; 2026 &mdash; Built with Java %s &amp; Docker</footer>
            </div>
            </body>
            </html>
            """.formatted(
                VERSION, VERSION, uptime,
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory() / (1024 * 1024),
                VERSION, now,
                System.getProperty("java.version")
            );
    }

    private static String healthPage() {
        return """
            {"status": "UP", "version": "%s", "timestamp": "%s", "uptime": "%s"}
            """.formatted(VERSION, LocalDateTime.now(), getUptime());
    }

    private static String apiInfo() {
        return """
            {
              "app": "Docker Training Java App",
              "version": "%s",
              "java": "%s",
              "os": "%s",
              "arch": "%s",
              "cpus": %d,
              "maxMemoryMB": %d,
              "uptime": "%s",
              "timestamp": "%s"
            }
            """.formatted(
                VERSION,
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory() / (1024 * 1024),
                getUptime(),
                LocalDateTime.now()
            );
    }

    private static String getUptime() {
        long seconds = (System.currentTimeMillis() - START_TIME) / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return h > 0 ? "%dh %dm %ds".formatted(h, m, s) : m > 0 ? "%dm %ds".formatted(m, s) : "%ds".formatted(s);
    }
}
