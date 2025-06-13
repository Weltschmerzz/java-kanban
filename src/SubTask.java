package ru.yandex.practicum.TaskTracker.src;

public class SubTask extends Epic {
    int subtaskId;
    int epicId;
    String subtaskName;
    String subtaskDescription;
    TaskStatus subtaskStatus;

    public SubTask(Epic epic, String subtaskName, String subtaskDescription, TaskStatus subtaskStatus) {
        super(epic.epicName, epic.epicDescription);
        this.epicId = epic.epicId;
        this.subtaskDescription = subtaskDescription;
        this.subtaskName = subtaskName;
        this.subtaskStatus = subtaskStatus;
    }

    public void setSubtaskId(int subtaskId) {
        this.subtaskId = subtaskId;
    }

    public int getSubtaskId() {
        return subtaskId;
    }

    @Override
    public int getEpicId() {
        return epicId;
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
