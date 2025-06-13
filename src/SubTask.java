package ru.yandex.practicum.TaskTracker.src;

public class SubTask extends Epic {
    int subtaskId;
    int epicId;
    String subtaskName;
    String subtaskDescription;
    TaskStatus subtaskStatus;

    public SubTask(Epic epic, int subtaskId, String subtaskName, String subtaskDescription, TaskStatus subtaskStatus) {
        super(epic.epicId, epic.epicName, epic.epicDescription, epic.epicStatus);
        this.epicId = epic.epicId;
        this.subtaskDescription = subtaskDescription;
        this.subtaskId = subtaskId;
        this.subtaskName = subtaskName;
        this.subtaskStatus = subtaskStatus;
    }


    @Override
    public String toString() {
        return "SubTask{" +
                "epicId=" + epicId +
                ", epicName='" + epicName + '\'' +
                ", epicStatus=" + epicStatus +
                ", subtaskId=" + subtaskId +
                ", subtaskName='" + subtaskName + '\'' +
                ", subtaskDescription='" + subtaskDescription + '\'' +
                ", subtaskStatus=" + subtaskStatus +
                '}';
    }
}
