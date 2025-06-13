package ru.yandex.practicum.TaskTracker.src;


public class Task {
    int taskId;
    String taskName;
    String taskDescription;
    TaskStatus taskStatus;


    public Task(int taskId, String taskName, String taskDescription, TaskStatus taskStatus) {
        this.taskDescription = taskDescription;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
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
