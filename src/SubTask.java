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

    public static SubTask fromFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        int epicId = Integer.parseInt(fields[5]);

        SubTask subtask = new SubTask(epicId, name, description, status);
        subtask.setId(id);
        return subtask;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return this.getId() + "," + "SUBTASK" + "," + this.getName() + "," + this.getStatus() + "," + this.getDescription() + "," + epicId;
    }
}
