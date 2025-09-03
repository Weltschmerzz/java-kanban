package ru.yandex.practicum.TaskTracker.src;


import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    PrioritizedHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();
        final String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                default -> sendNotFound(exchange);
            }
        } catch (RuntimeException e) {
            sendError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (isRootPath(path, "/prioritized")) {
            List<Task> prioritized = manager.getPrioritizedTasks();
            sendOk(exchange, gson.toJson(prioritized));
            return;
        }
        sendNotFound(exchange);
    }

}