package ru.yandex.practicum.TaskTracker.src;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subTaskIds = new ArrayList<>();
    private Duration durationEpic = Duration.ZERO;
    private LocalDateTime startTimeEpic;
    private LocalDateTime endTimeEpic;

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
        return endTimeEpic;
    }

    @Override
    public Duration getDuration() {
        return durationEpic;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTimeEpic;
    }

    public void setDuration(Duration duration) {
        this.durationEpic = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTimeEpic = startTime;
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
                + (this.durationEpic == null ? "" : durationEpic.toMinutes()) + ","
                + (this.startTimeEpic == null ? "" : startTimeEpic.format(CSV_DATE_TIME));
    }

    void setCalculatedTime(LocalDateTime start, LocalDateTime end, Duration duration) {
        this.startTimeEpic = start;
        this.endTimeEpic = end;
        this.durationEpic = duration;
    }
}

