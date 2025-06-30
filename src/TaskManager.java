package ru.yandex.practicum.TaskTracker.src;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    void createTask(Task newTask);

    void createEpic(Epic newEpic);

    void createSubTask(SubTask newSubTask);

    ArrayList<Task> getTaskList();

    ArrayList<Epic> getEpicList();

    ArrayList<SubTask> getSubTaskList();

    ArrayList<SubTask> getSubTaskListByEpic(int epicId);

    void clearTaskList();

    void clearEpicList();

    void clearSubTaskList();

    void clearSubTaskListForEpic(int epicId);

    void updateTask(Task updatedTask);

    void updateEpic(Epic updatedEpic);

    void updateSubTask(SubTask updatedSubTask);

    void deleteTask(int id);

    void deleteEpic(int epicId);

    void deleteSubTask(int subTaskId);

    Task getTaskById(int taskId);

    Epic getEpicById(int epicId);

    SubTask getSubTaskById(int subTaskId);

    List<Task> getHistory();
}
