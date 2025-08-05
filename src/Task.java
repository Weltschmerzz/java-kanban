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

    public static Task fromFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        Task task = new Task(name, description, status);
        task.setId(id);
        return task;
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
