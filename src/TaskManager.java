package ru.yandex.practicum.TaskTracker.src;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int id = 0;
    private final HashMap<Integer, Task> taskList;
    private final HashMap<Integer, Epic> epicList;
    private final HashMap<Integer, SubTask> subTaskList;

    public TaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subTaskList = new HashMap<>();
    }

    public void createTask(Task newTask) {
        int newTaskid = getTaskid();
        newTask.setId(newTaskid);
        taskList.put(newTaskid, newTask);
        System.out.println("Задачи с ID:" + newTaskid + " успешно создана!");
    }

    public void createEpic(Epic newEpic) {
        int newTaskid = getTaskid();
        newEpic.setId(newTaskid);
        epicList.put(newTaskid, newEpic);
        System.out.println("Эпик с ID: " + newTaskid + " успешно создан!");

    }

    public void createSubTask(SubTask newSubTask) {
        int newSubTaskid = getTaskid();
        newSubTask.setId(newSubTaskid);
        subTaskList.put(newSubTaskid, newSubTask);
        epicList.get(newSubTask.getEpicId()).addSubTaskId(newSubTaskid);
        epicStatusCalculate(newSubTask.getEpicId());
    }

    public ArrayList<Task> getTaskList() {
        if (taskList.isEmpty()) {
            System.out.println("Список задач пуст!");
            System.out.println("*".repeat(25));
        }
        return new ArrayList<>(taskList.values());
    }

    public ArrayList<Epic> getEpicList() {
        if (epicList.isEmpty()) {
            System.out.println("Список эпиков пуст!");
            System.out.println("*".repeat(25));
        }
        return new ArrayList<>(epicList.values());
    }

    public ArrayList<SubTask> getSubTaskList() {
        if (subTaskList.isEmpty()) {
            System.out.println("Список подзадач пуст!");
            System.out.println("*".repeat(25));
        }
        return new ArrayList<>(subTaskList.values());
    }

    public ArrayList<SubTask> getSubTaskListByEpic(int epicId) {
        boolean hasEpicSubTask = false;
        ArrayList<SubTask> subTaskForOneEpic = new ArrayList<>();

        for (Integer id : epicList.get(epicId).getSubTaskIds()) {
            if (id != null) {
                subTaskForOneEpic.add(subTaskList.get(id));
                hasEpicSubTask = true;
            }
        }
        if (!hasEpicSubTask) {
            System.out.println("У данного эпика нет подзадач!");
            System.out.println("*".repeat(25));
        }
        return subTaskForOneEpic;
    }

    public void clearTaskList() {
        taskList.clear();
        System.out.println("Все задачи удалены!");
        System.out.println("*".repeat(25));
    }

    public void clearEpicList() {
        epicList.clear();
        System.out.println("Все Эпики удалены!");
        subTaskList.clear();
        System.out.println("Все Подзадачи удалены!");
        System.out.println("*".repeat(25));
    }

    public void clearSubTaskList() {
        subTaskList.clear();
        System.out.println("Все подзадачи удалены!");

        for (Epic epic : epicList.values()) {
            epicStatusCalculate(epic.getId());
        }
    }

    public void clearSubTaskListForEpic(int epicId) {
        ArrayList<Integer> removeCandidate = new ArrayList<>();
        for (SubTask sTask : subTaskList.values()) {
            if (sTask.getEpicId() == epicId) {
                removeCandidate.add(sTask.getId());
            }
        }
        for (Integer i : removeCandidate) {
            subTaskList.remove(i);
        }
        epicList.get(epicId).getSubTaskIds().clear();
        System.out.println("Все подзадачи для эпика id:" + epicId + " удалены!");
        epicStatusCalculate(epicId);
    }

    public void updateTask(Task updatedTask) {
        taskList.put(updatedTask.getId(), updatedTask);
    }

    public void updateEpic(Epic updatedEpic) {
        epicList.put(updatedEpic.getId(), updatedEpic);
    }

    public void updateSubTask(SubTask updatedSubTask) {
        subTaskList.put(updatedSubTask.getId(), updatedSubTask);
        epicStatusCalculate(updatedSubTask.getEpicId());
    }

    public void deleteTask(int id) {
        taskList.remove(id);
        System.out.println("Задача ID:" + id + " удалена.");
        System.out.println("*".repeat(25));
    }

    public void deleteEpic(int epicId) {
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);
        System.out.println("Эпик ID:" + epicId + " удален.");
        System.out.println("*".repeat(25));
    }

    public void deleteSubTask(int subTaskId) {
        int epicId = subTaskList.get(subTaskId).getEpicId();
        subTaskList.remove(subTaskId);
        epicList.get(epicId).removeSubTaskId(subTaskId);
        System.out.println("Подзадача ID:" + subTaskId + " удалена.");
        System.out.println("*".repeat(25));
        epicStatusCalculate(epicId);
    }

    public Task getTaskById(int taskId) {
        return taskList.get(taskId);
    }

    public Epic getEpicById(int epicId) {
        return epicList.get(epicId);
    }

    public SubTask getSubTaskById(int subTaskId) {
        return subTaskList.get(subTaskId);
    }

    private int getTaskid() {
        id++;
        return id;
    }

    private void epicStatusCalculate(int epicId) {
        TaskStatus calculatedStatus = TaskStatus.IN_PROGRESS;
        int countSubTask = 0;
        int countNewStatus = 0;
        int countDoneStatus = 0;

        for (Integer subTaskId : epicList.get(epicId).getSubTaskIds()) {
            SubTask subTask = subTaskList.get(subTaskId);
            if (subTask != null) {
                countSubTask++;
                if (subTask.getStatus().equals(TaskStatus.NEW)) {
                    countNewStatus++;
                } else if (subTask.getStatus().equals(TaskStatus.DONE)) {
                    countDoneStatus++;
                }
            }
        }
        if (countSubTask == 0) {
            calculatedStatus = TaskStatus.NEW;
        } else if (countNewStatus == countSubTask) {
            calculatedStatus = TaskStatus.NEW;
        } else if (countDoneStatus == countSubTask) {
            calculatedStatus = TaskStatus.DONE;
        }
        epicList.get(epicId).setStatus(calculatedStatus);
    }
}
