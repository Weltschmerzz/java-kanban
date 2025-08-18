package ru.yandex.practicum.TaskTracker.src;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subTaskIds = new ArrayList<>();
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public static Epic fromFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        Epic epic = new Epic(name, description);
        epic.setId(id);
        epic.setStatus(status);
        return epic;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void setSubTaskIds(List<Integer> copySubTaskIds) {
        if (copySubTaskIds != null) {
            subTaskIds.addAll(copySubTaskIds);
        }
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void removeSubTaskId(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }

    @Override
    public String toString() {
        return this.getId() + "," + TaskType.EPIC + "," + this.getName() + "," + this.getStatus() + "," + this.getDescription() + "" + ","
                + (this.duration == null ? "" : duration.toMinutes()) + ","
                + (this.startTime == null ? "" : startTime.format(CSV_DATE_TIME));
    }

    void setCalculatedTime(LocalDateTime start, LocalDateTime end, Duration duration) {
        this.startTime = start;
        this.endTime = end;
        this.duration = duration;
    }
}

