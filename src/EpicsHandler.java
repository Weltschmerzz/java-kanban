package ru.yandex.practicum.TaskTracker.src;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendError(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendError(exchange);
        } catch (IllegalStateException e) {
            sendOverlap(exchange);
        } catch (RuntimeException e) {
            sendError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (isRootPath(path, "/epics")) {
            List<Epic> allEpics = manager.getEpicList();
            sendOk(exchange, gson.toJson(allEpics));
            return;
        }

        Optional<Integer> idOpt1 = extractId(path, "/epics");
        if (idOpt1.isPresent()) {
            int id = idOpt1.get();
            Epic epic = manager.getEpicById(id);

            if (epic == null) {
                sendNotFound(exchange);
                return;
            }

            sendOk(exchange, gson.toJson(epic));
            return;
        }

        Optional<Integer> idOpt2 = extractIdFromMiddleOfPath(path, "/epics");
        boolean isSubtasksTail = path.endsWith("/subtasks") || path.endsWith("/subtasks/");
        if (idOpt2.isPresent() && isSubtasksTail) {
            int id = idOpt2.get();

            Epic epic = manager.getEpicById(id);

            if (epic == null) {
                sendNotFound(exchange);
                return;
            }

            List<Integer> subTaskIds = epic.getSubTaskIds();
            List<SubTask> subTasksFromEpic = subTaskIds.stream()
                    .map(manager::getSubTaskById)
                    .filter(Objects::nonNull)
                    .toList();

            sendOk(exchange, gson.toJson(subTasksFromEpic));
            return;
        }
        sendNotFound(exchange);
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!isRootPath(path, "/epics")) {
            sendNotFound(exchange);
            return;
        }

        String body = readBody(exchange);
        Epic newEpic = gson.fromJson(body, Epic.class);

        if (newEpic == null || newEpic.getName() == null || newEpic.getName().isBlank()) {
            sendBadRequest(exchange, "Некорректный запрос!");
            return;
        }

        if (newEpic.getId() == 0) {
            Epic tempEpic = new Epic(newEpic.getName(), newEpic.getDescription());
            manager.createEpic(tempEpic);

            String responseBody = "Эпик id: " + tempEpic.getId() + " создан!";
            sendUpdated(exchange, responseBody);
            return;
        }

        Epic existing = manager.getEpicById(newEpic.getId());

        if (existing == null) {
            sendNotFound(exchange);
            return;
        }

        manager.updateEpic(newEpic);
        String responseBody = "Эпик id: " + newEpic.getId() + " обновлён!";
        sendUpdated(exchange, responseBody);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Optional<Integer> idOpt = extractId(path, "/epics");

        if (idOpt.isPresent()) {
            int id = idOpt.get();

            if (manager.getEpicById(id) == null) {
                sendNotFound(exchange);
                return;
            }

            manager.deleteEpic(id);
            String responseBody = "Эпик ID: " + id + " удален!";
            sendUpdated(exchange, responseBody);
            return;
        }

        if (isRootPath(path, "/epics")) {
            manager.clearEpicList();
            String responseBody = "Все эпики удалены!";
            sendUpdated(exchange, responseBody);
            return;
        }

        sendNotFound(exchange);
    }
}
