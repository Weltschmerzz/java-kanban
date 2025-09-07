package ru.yandex.practicum.TaskTracker.src;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager, Gson gson) {
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
        if (isRootPath(path, "/tasks")) {
            List<Task> allTask = manager.getTaskList();
            sendOk(exchange, gson.toJson(allTask));
            return;
        }

        Optional<Integer> idOpt = extractId(path, "/tasks");
        if (idOpt.isEmpty()) {
            sendNotFound(exchange);
            return;
        }

        int id = idOpt.get();

        Task task = manager.getTaskById(id);
        if (task == null) {
            sendNotFound(exchange);
            return;
        }
        sendOk(exchange, gson.toJson(task));
    }


    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!isRootPath(path, "/tasks")) {
            sendNotFound(exchange);
            return;
        }

        String body = readBody(exchange);
        Task newTask = gson.fromJson(body, Task.class);

        if (newTask == null || newTask.getName() == null || newTask.getName().isBlank()) {
            sendBadRequest(exchange, "Имя задачи не найдено!");
            return;
        }

        if (newTask.getStatus() == null) {
            newTask.setStatus(TaskStatus.NEW);
        }

        Integer id = newTask.getId();

        if (id == null || id == 0) {
            manager.createTask(newTask);
            String responseBody = "Задача id: " + newTask.getId() + " создана!";
            sendUpdated(exchange, responseBody);
            return;
        }

        Task existingTask = manager.getTaskById(id);
        if (existingTask == null) {
            sendNotFound(exchange);
            return;
        }

        manager.updateTask(newTask);
        sendUpdated(exchange, "Задача id: " + id + " обновлена!");
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (isRootPath(path, "/tasks")) {
            manager.clearTaskList();
            String responseBody = "Все задачи удалены!";
            sendUpdated(exchange, responseBody);
            return;
        }

        Optional<Integer> idOpt = extractId(path, "/tasks");

        if (idOpt.isPresent()) {
            int id = idOpt.get();
            if (manager.getTaskById(id) == null) {
                sendNotFound(exchange);
                return;
            }
            manager.deleteTask(id);
            String responseBody = "Задача с ID: " + id + " удалена!";
            sendUpdated(exchange, responseBody);
        }
    }
}
