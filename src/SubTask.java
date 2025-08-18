package ru.yandex.practicum.TaskTracker.src;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(Epic epic, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epic.getId();
    }

    public SubTask(String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public static SubTask fromFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        int epicId = Integer.parseInt(fields[5]);
        String StrDuration = fields[6];
        String strStartTime = fields[7];

        Duration duration = StrDuration.isBlank() ? null : Duration.ofMinutes(Long.parseLong(StrDuration));
        LocalDateTime startTime = strStartTime.isBlank() ? null : LocalDateTime.parse(strStartTime, CSV_DATE_TIME);

        SubTask subtask = new SubTask(name, description, status, startTime, duration, epicId);
        subtask.setId(id);
        return subtask;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return this.getId() + "," + TaskType.SUBTASK + "," + this.getName() + "," + this.getStatus() + "," + this.getDescription() + "," + epicId + ","
                + (getDuration() == null ? "" : getDuration().toMinutes()) + ","
                + (getStartTime() == null ? "" : getStartTime().format(CSV_DATE_TIME));
    }
}
