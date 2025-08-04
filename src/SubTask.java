package ru.yandex.practicum.TaskTracker.src;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(Epic epic, String name, String description, TaskStatus status) {
        super(name, description, status);
        this.epicId = epic.getId();
    }

    public SubTask(int epicId, String name, String description, TaskStatus status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return this.getId() + "," + "SUBTASK" + "," + this.getName() + "," + this.getStatus() + "," + this.getDescription() + "," + epicId;
    }
}
