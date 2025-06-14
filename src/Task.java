package ru.yandex.practicum.TaskTracker.src;


public class Task {
    int taskId;
    String taskName;
    String taskDescription;
    TaskStatus taskStatus;


    public Task(String taskName, String taskDescription, TaskStatus taskStatus) {
        this.taskDescription = taskDescription;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", taskDescription='" + taskDescription + '\'' +
                ", taskStatus=" + taskStatus +
                '}';
    }
}
