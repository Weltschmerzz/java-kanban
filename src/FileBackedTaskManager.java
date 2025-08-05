package ru.yandex.practicum.TaskTracker.src;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final Path storageFile;

    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager fileManager = new FileBackedTaskManager(path);

        if (Files.exists(fileManager.storageFile)) {
            fileManager.load();
        } else {
            try {
                fileManager.createTemplateCSV(fileManager.storageFile);
            } catch (FileManagerException e) {
                System.out.println(e.getMessage() + "\nПричина: " + e.getCause());
            }
        }
        return fileManager;
    }

    public FileBackedTaskManager(Path path) throws FileManagerException {
        storageFile = path;
    }

    @Override
    public void clearEpicList() {
        super.clearEpicList();
        save();
    }

    @Override
    public void clearSubTaskList() {
        super.clearSubTaskList();
        save();
    }

    @Override
    public void clearSubTaskListForEpic(int epicId) {
        super.clearSubTaskListForEpic(epicId);
        save();
    }

    @Override
    public void clearTaskList() {
        super.clearTaskList();
        save();
    }

    @Override
    public void createEpic(Epic newEpic) {
        super.createEpic(newEpic);
        save();
    }

    @Override
    public void createSubTask(SubTask newSubTask) {
        super.createSubTask(newSubTask);
        save();
    }

    @Override
    public void createTask(Task newTask) {
        super.createTask(newTask);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        super.deleteSubTask(subTaskId);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        super.updateEpic(updatedEpic);
        save();
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        super.updateSubTask(updatedSubTask);
        save();
    }

    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    private void createTemplateCSV(Path filePath) throws FileManagerException {
        try {
            Files.createDirectories(filePath.getParent());

            if (!Files.exists(filePath)) {
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(storageFile.toFile()), StandardCharsets.UTF_8)) {
                    writer.write("id,type,name,status,description,epic\n");
                }
            }
        } catch (IOException e) {
            throw new FileManagerException("Ошибка при создании CSV-файла", e);
        }
    }

    private void load() throws FileManagerException {
        try {
            List<String> rows = Files.readAllLines(storageFile);
            for (int i = 1; i < rows.size(); i++) {
                String line = rows.get(i);
                if (line.isEmpty()) {
                    continue;
                }
                Task task = fromString(line);
                restore(task);
            }
            updateIdCounterAfterLoad();
        } catch (IOException e) {
            throw new FileManagerException("Ошибка при чтении CSV-файла", e);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new FileManagerException("Ошибка восстановления данных из файла", e);
        }
    }

    public Task fromString(String line) throws IllegalArgumentException {
        String[] fields = line.split(",");
        TaskType type = TaskType.valueOf(fields[1]);

        switch (type) {
            case TASK:
                return Task.fromFields(fields);
            case EPIC:
                return Epic.fromFields(fields);
            case SUBTASK:
                return SubTask.fromFields(fields);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(storageFile.toFile()), StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getTaskList()) {
                writer.write(task.toString() + "\n");
            }

            for (Epic epic : getEpicList()) {
                writer.write(epic.toString() + "\n");
            }

            for (SubTask subTask : getSubTaskList()) {
                writer.write(subTask.toString() + "\n");
            }

        } catch (IOException e) {
            System.out.println("Ошибка при записи в CSV-файл: " + e.getMessage());
        }
    }

    private void restore(Task task) throws FileManagerException {
        if (task instanceof SubTask) {
            getSubTaskMap().put(task.getId(), (SubTask) task);
            Epic epic = getEpicMap().get(((SubTask) task).getEpicId());
            if (epic == null) {
                throw new FileManagerException("Ошибка восстановления: не найден эпик с id " + ((SubTask) task).getEpicId());
            }
            epic.addSubTaskId(task.getId());
        } else if (task instanceof Epic) {
            getEpicMap().put(task.getId(), (Epic) task);
        } else {
            getTaskMap().put(task.getId(), task);
        }
    }
}
