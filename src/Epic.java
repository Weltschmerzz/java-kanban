package ru.yandex.practicum.TaskTracker.src;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void setSubTaskIds(ArrayList<Integer> copySubTaskIds) {
        if (copySubTaskIds != null) {
            subTaskIds.addAll(copySubTaskIds);
        }
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void removeSubTaskId(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }
}

