package ru.yandex.practicum.TaskTracker.src;

public class Epic {
    int epicId;
    String epicName;
    String epicDescription;
    TaskStatus epicStatus;

    public Epic(String epicName, String epicDescription) {
        this.epicDescription = epicDescription;
        this.epicName = epicName;
        this.epicStatus = TaskStatus.NEW;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
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
