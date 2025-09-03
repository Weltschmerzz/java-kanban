package ru.yandex.practicum.TaskTracker.src;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SubTaskHandler extends BaseHttpHandler {

    SubTaskHandler(TaskManager manager, Gson gson) {
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
        if (isRootPath(path, "/subtasks")) {
            List<SubTask> allSubTask = manager.getSubTaskList();
            sendOk(exchange, gson.toJson(allSubTask));
        }

        Optional<Integer> idOpt = extractId(path, "/subtasks");
        if (idOpt.isEmpty()) {
            sendError(exchange);
            return;
        }

        int id = idOpt.get();
        Task subTask = manager.getSubTaskById(id);
        if (subTask == null) {
            sendNotFound(exchange);
            return;
        }
        sendOk(exchange, gson.toJson(subTask));
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!isRootPath(path, "/subtasks")) {
            sendNotFound(exchange);
            return;
        }

        try {

            String body = readBody(exchange);
            SubTask newSubTask = gson.fromJson(body, SubTask.class);

            if (newSubTask == null || newSubTask.getName() == null || newSubTask.getName().isBlank()) {
                sendBadRequest(exchange, "Имя подзадачи обязательно.");
                return;
            }

            if (newSubTask.getStatus() == null) {
                newSubTask.setStatus(TaskStatus.NEW);
            }

            int epicId = newSubTask.getEpicId();
            if (epicId <= 0) {
                sendBadRequest(exchange, "epicId должен быть > 0.");
                return;
            }

            Epic epic = manager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange);
                return;
            }

            int id = newSubTask.getId();

            if (id == epicId) {
                sendBadRequest(exchange, "id подзадачи не может совпадать с id эпика.");
                return;
            }

            if (id == 0) {
                SubTask addSubTask = new SubTask(newSubTask.getName(),
                        newSubTask.getDescription(),
                        newSubTask.getStatus(),
                        newSubTask.getStartTime(),
                        newSubTask.getDuration(),
                        epicId);

                manager.createSubTask(addSubTask);
                String responseBody = "Подзадача id: " + addSubTask.getId() + " создана для Эпика id: " + epicId;
                sendUpdated(exchange, responseBody);
                return;
            }

            SubTask existingSubTask = manager.getSubTaskById(id);

            if (existingSubTask == null) {
                sendNotFound(exchange);
                return;
            }
            if (existingSubTask.getEpicId() != epicId) {
                sendBadRequest(exchange, "Нельзя менять epicId у подзадачи через обновление.");
                return;
            }

            SubTask updatedSubTask = new SubTask(newSubTask.getName(),
                    newSubTask.getDescription(),
                    newSubTask.getStatus(),
                    newSubTask.getStartTime(),
                    newSubTask.getDuration(),
                    epicId);

            updatedSubTask.setId(id);
            manager.updateSubTask(updatedSubTask);

            String responseBoady = "Подзадача id: " + updatedSubTask.getId() + " обновлена для Эпика id: " + updatedSubTask.getEpicId();
            sendUpdated(exchange, responseBoady);

        } catch (IllegalStateException overlap) {
            sendOverlap(exchange);
        } catch (JsonSyntaxException badJson) {
            sendBadRequest(exchange, "Некорректный JSON: " + badJson.getMessage());
        } catch (RuntimeException ex) {
            sendError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (isRootPath(path, "/subtasks")) {
            manager.clearSubTaskList();
            String responseBody = "Все подзадачи удалены!";
            sendUpdated(exchange, responseBody);
            return;
        }

        Optional<Integer> idOpt = extractId(path, "/subtasks");

        if (idOpt.isPresent()) {
            int id = idOpt.get();
            if (manager.getSubTaskById(id) == null) {
                sendNotFound(exchange);
                return;
            }
            manager.deleteSubTask(id);
            String responseBody = "Подзадача с ID: " + id + " удалена!";
            sendUpdated(exchange, responseBody);
        }
    }
}
