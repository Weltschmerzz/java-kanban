package ru.yandex.practicum.TaskTracker.src;

import java.util.Objects;

public class Task {

    private int id;
    private final String name;
    private final String description;
    private TaskStatus status;

    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    void setStatus(TaskStatus status) {
        this.status = status;
    }

    public static Task fromString(String line) {
        String[] taskArr = line.split(",", -1);
        int id = Integer.parseInt(taskArr[0]);
        TaskType type = TaskType.valueOf(taskArr[1]);
        String name = taskArr[2];
        TaskStatus status = TaskStatus.valueOf(taskArr[3]);
        String description = taskArr[4];
        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(taskArr[5]);
                SubTask subtask = new SubTask(epicId, name, description, status);
                subtask.setId(id);
                return subtask;
            default:
                System.out.println("Неизвестный тип задачи: " + type);
                return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status);
    }

    @Override
    public String toString() {
        return id + "," + "TASK" + "," + name + "," + status + "," + description + ",";
    }


}
