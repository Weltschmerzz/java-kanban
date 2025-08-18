package ru.yandex.practicum.TaskTracker.src;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {

    private int id;
    private final String name;
    private final String description;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;
    static final DateTimeFormatter CSV_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public Task(String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

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
        String StrDuration = fields[6];
        String strStartTime = fields[7];

        Duration duration = StrDuration.isBlank() ? null : Duration.ofMinutes(Long.parseLong(StrDuration));
        LocalDateTime startTime = strStartTime.isBlank() ? null : LocalDateTime.parse(strStartTime, CSV_DATE_TIME);

        Task task = new Task(name, description, status, startTime, duration);
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

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) return null;
        return startTime.plus(duration);
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
        return id + "," + TaskType.TASK + "," + name + "," + status + "," + description + "," + "" + ","
                + (duration == null ? "" : duration.toMinutes()) + ","
                + (startTime == null ? "" : startTime.format(CSV_DATE_TIME));
    }


}
