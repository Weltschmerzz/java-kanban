package ru.yandex.practicum.TaskTracker.src;

public class Epic extends Task {

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    protected void updateStatus(TaskStatus status) {
        this.status = status;
    }
}

