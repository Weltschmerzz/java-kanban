package ru.yandex.practicum.TaskTracker.src;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(Epic epic, String name, String description, TaskStatus status) {
        super(name, description, status);
        this.epicId = epic.getId();
    }

    public int getEpicId() {
        return epicId;
    }

    public String toString() {
        return "{" +
                "EpicId=" + epicId +
                ", SubTaskId=" + getId() +
                ", Name='" + getName() + '\'' +
                ", Description='" + getDescription() + '\'' +
                ", Status=" + getStatus() +
                '}';
    }
}
