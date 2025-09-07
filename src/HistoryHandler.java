package ru.yandex.practicum.TaskTracker.src;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

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
        if (isRootPath(path, "/history")) {
            List<Task> history = manager.getHistory();
            sendOk(exchange, gson.toJson(history));
            return;
        }

        sendNotFound(exchange);
    }
}
