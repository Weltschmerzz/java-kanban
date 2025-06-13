package ru.yandex.practicum.TaskTracker.src;

public class Epic {
    int epicId;
    String epicName;
    String epicDescription;
    TaskStatus epicStatus;

    public Epic(int epicId, String epicName, String epicDescription, TaskStatus epicStatus) {
        this.epicDescription = epicDescription;
        this.epicId = epicId;
        this.epicName = epicName;
        this.epicStatus = epicStatus;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "epicId=" + epicId +
                ", epicName='" + epicName + '\'' +
                ", epicDescription='" + epicDescription + '\'' +
                ", epicStatus=" + epicStatus +
                '}';
    }
}
