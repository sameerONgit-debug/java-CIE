package com.openaircafeteria;

import com.openaircafeteria.model.Order;
import com.openaircafeteria.service.CanteenService;
import com.openaircafeteria.service.JsonUtil;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Minimal server using only the JDK. It serves the React page and two small API
 * routes: GET /api/menu and POST /api/orders.
 */
public class CanteenServer {
    private final CanteenService canteenService;
    private final Path webRoot;

    public CanteenServer(Path webRoot) {
        this.canteenService = new CanteenService();
        this.webRoot = webRoot.toAbsolutePath().normalize();
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Open Air Cafeteria is running at http://localhost:" + port);
    }

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange.getResponseHeaders());
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            String path = exchange.getRequestURI().getPath();
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())
                        && "/api/menu".equals(path)) {
                    sendJson(exchange, 200, canteenService.getMenuResponse());
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())
                        && "/api/orders".equals(path)) {
                    sendJson(exchange, 200, canteenService.getOrders());
                    return;
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())
                        && "/api/orders".equals(path)) {
                    createOrder(exchange);
                    return;
                }

                sendError(exchange, 404, "API route not found");
            } catch (IllegalArgumentException exception) {
                sendError(exchange, 400, exception.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
                sendError(exchange, 500, "Something went wrong on the server");
            }
        }

        @SuppressWarnings("unchecked")
        private void createOrder(HttpExchange exchange) throws IOException {
            String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Object parsed = JsonUtil.parse(body);
            if (!(parsed instanceof Map)) {
                throw new IllegalArgumentException("Request must be a JSON object");
            }

            Map<String, Object> request = (Map<String, Object>) parsed;
            Map<String, Integer> items = new LinkedHashMap<String, Integer>();
            Object rawItems = request.get("items");
            if (rawItems instanceof List) {
                for (Object rawItem : (List<Object>) rawItems) {
                    if (!(rawItem instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> item = (Map<String, Object>) rawItem;
                    String id = String.valueOf(item.get("id"));
                    Object rawQuantity = item.get("quantity");
                    int quantity = rawQuantity instanceof Number
                            ? ((Number) rawQuantity).intValue() : 0;
                    items.put(id, quantity);
                }
            }

            Order order = canteenService.placeOrder(
                    value(request, "name"),
                    value(request, "email"),
                    value(request, "pickupSlot"),
                    value(request, "notes"),
                    items
            );

            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("message", "Order placed successfully");
            response.put("order", order.toMap());
            sendJson(exchange, 201, response);
        }

        private String value(Map<String, Object> values, String key) {
            Object value = values.get(key);
            return value == null ? "" : String.valueOf(value);
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            String relativePath = requestPath == null || "/".equals(requestPath)
                    ? "index.html" : URLDecoder.decode(requestPath.substring(1), "UTF-8");
            Path file = webRoot.resolve(relativePath).normalize();

            if (!file.startsWith(webRoot) || !Files.exists(file) || Files.isDirectory(file)) {
                sendText(exchange, 404, "Page not found", "text/plain; charset=utf-8");
                return;
            }

            String contentType = contentType(file.toString());
            byte[] content = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(content);
            }
        }
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (fileName.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (fileName.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "application/octet-stream";
    }

    private static void addCorsHeaders(Headers headers) {
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private static void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        sendText(exchange, status, JsonUtil.stringify(value), "application/json; charset=utf-8");
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, Object> error = new LinkedHashMap<String, Object>();
        error.put("error", message == null ? "Unknown error" : message);
        sendJson(exchange, status, error);
    }

    private static void sendText(HttpExchange exchange, int status, String text, String contentType)
            throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                System.out.println("Invalid port. Using 8080.");
            }
        }
        new CanteenServer(Paths.get("web")).start(port);
    }
}
