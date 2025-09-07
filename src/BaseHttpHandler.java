package ru.yandex.practicum.TaskTracker.src;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    protected void sendText(HttpExchange h, int code, String json) throws IOException {
        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendOk(HttpExchange h, String json) throws IOException {
        sendText(h, 200, json);
    }

    protected void sendUpdated(HttpExchange h, String message) throws IOException {
        String json = gson.toJson(java.util.Map.of("message", message));
        sendText(h, 201, json);
    }

    protected void sendBadRequest(HttpExchange h, String message) throws IOException {
        String json = gson.toJson(java.util.Map.of("message", message));
        sendText(h, 400, json);
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        String json = gson.toJson(Map.of("error", "Not Found"));
        sendText(h, 404, json);
    }

    protected void sendOverlap(HttpExchange h) throws IOException {
        String json = gson.toJson(Map.of("error", "Not Acceptable"));
        sendText(h, 406, json);
    }

    protected void sendError(HttpExchange h) throws IOException {
        String json = gson.toJson(Map.of("error", "Internal Server Error"));
        sendText(h, 500, json);
    }

    protected String readBody(HttpExchange h) throws IOException {
        try (InputStream is = h.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected boolean isRootPath(String path, String basePath) {
        return path.equals(basePath) || path.equals(basePath + "/");
    }

    protected Optional<Integer> extractId(String path, String basePath) {
        String prefix = basePath + "/";

        if (!path.startsWith(prefix)) {
            return Optional.empty();
        }

        String shouldBeId = path.substring(prefix.length());
        int slash = shouldBeId.indexOf('/');
        if (slash != -1 || shouldBeId.isBlank()) {
            return Optional.empty();
        }

        try {
            int id = Integer.parseInt(shouldBeId);
            return (id >= 0) ? Optional.of(id) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    protected Optional<Integer> extractIdFromMiddleOfPath(String path, String basePath) {
        String prefix = basePath + "/";

        if (!path.startsWith(prefix)) {
            return Optional.empty();
        }

        int nextSlash = path.indexOf("/", prefix.length());
        String idString;

        if (nextSlash == -1) {
            idString = path.substring(prefix.length());
        } else {
            idString = path.substring(prefix.length(), nextSlash);
        }

        if (idString.isBlank()) {
            return Optional.empty();
        }

        try {
            int id = Integer.parseInt(idString);

            if (id < 0) {
                return Optional.empty();
            }

            return Optional.of(id);

        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

