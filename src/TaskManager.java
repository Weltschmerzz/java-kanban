package ru.yandex.practicum.TaskTracker.src;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
   private int id = 0;

   private HashMap<Integer, Task> taskList;
   private HashMap<Integer, Epic> epicList;
   private HashMap<Integer, SubTask> subTaskList;


   public TaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subTaskList = new HashMap<>();
    }


    private int getTaskid() {
        id++;
        return id;
    }




    public void createTask(Task newTask) {
        int newTaskid = getTaskid();
        newTask.setTaskId(newTaskid);
        taskList.put(newTaskid, newTask);
        System.out.println("Задачи с ID:" + newTaskid + " успешно создана!");
    }


    public void createEpic(Epic newEpic) {
        int newTaskid = getTaskid();
        newEpic.setEpicId(newTaskid);
        epicList.put(newTaskid, newEpic);
        System.out.println("Эпик с ID: " + newTaskid + " успешно создан!");

    }

    public void createSubTask(SubTask newSubTask) {
        int newSubTaskid = getTaskid();
        newSubTask.setSubtaskId(newSubTaskid);
        subTaskList.put(newSubTaskid, newSubTask);
        epicStatusCalculate(newSubTask.getEpicId());
    }

    private void epicStatusCalculate(int epicId) {
        TaskStatus calculatedStatus = TaskStatus.IN_PROGRESS;
        int countSubTask = 0;
        int countNewStatus = 0;
        int countDoneStatus = 0;
        int countInProgressStatus = 0;

        for (SubTask subTask : subTaskList.values()) {   // Если все subtask NEW
            if (subTask.epicId == epicId) {
                countSubTask++;
                if (subTask.subtaskStatus.equals(TaskStatus.NEW)) {
                    countNewStatus++;
                } else if (subTask.subtaskStatus.equals(TaskStatus.DONE)) {
                    countDoneStatus++;
                } else if (subTask.subtaskStatus.equals(TaskStatus.IN_PROGRESS)) {
                    countInProgressStatus++;
                }
            }
        }

        if (countSubTask == 0) {
            calculatedStatus = TaskStatus.NEW;
        } else if (countNewStatus == countSubTask) {
            calculatedStatus = TaskStatus.NEW;
        } else if (countDoneStatus == countSubTask) {
            calculatedStatus = TaskStatus.DONE;
        } else if (countInProgressStatus > 0) {
            calculatedStatus = TaskStatus.IN_PROGRESS;
        }

        epicList.get(epicId).epicStatus = calculatedStatus;
    }


    public HashMap<Integer, Task> getTaskList() { //Получить список задач
        if (taskList.isEmpty()) {
            System.out.println("Список задач пуст!");
            System.out.println("*".repeat(25));
        }

        return taskList;
    }

    public HashMap<Integer, Epic> getEpicList() {
        if (epicList.isEmpty()) {
            System.out.println("Список эпиков пуст!");
            System.out.println("*".repeat(25));
        }
        return epicList;
    }

    public HashMap<Integer, SubTask> getSubTaskList() {
        if (subTaskList.isEmpty()) {
            System.out.println("Список подзадач пуст!");
            System.out.println("*".repeat(25));
        }
        return subTaskList;
    }

    public ArrayList<SubTask> getSubTaskListByEpic(int epicId) {
        boolean hasEpicSubTask = false;
        ArrayList<SubTask> subTaskForOneEpic = new ArrayList<>();
        for (SubTask sTask : subTaskList.values()) {
            if (sTask.epicId == epicId) {
                subTaskForOneEpic.add(sTask);
                hasEpicSubTask = true;
            }
        }
        if (!hasEpicSubTask) {
            System.out.println("У данного эпика нет подзадач!");
            System.out.println("*".repeat(25));
        }
        return subTaskForOneEpic;
    }

    public void clearTaskList() {  //Удалить все задачи из списка
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
            epicStatusCalculate(epic.epicId);
        }
    }

    public void clearSubTaskListForEpic(int epicId) {
        ArrayList<Integer> removeCandidate = new ArrayList<>();
        for (SubTask sTask : subTaskList.values()) {
            if (sTask.epicId == epicId) {
                removeCandidate.add(sTask.subtaskId);
            }
        }

        for (int i = 0; i < removeCandidate.size(); i++) {
            subTaskList.remove(removeCandidate.get(i));
        }

        boolean check = true;
        for (SubTask sTask : subTaskList.values()) {
            if (sTask.epicId == epicId) {
                if (subTaskList.containsValue(sTask.subtaskId)) {
                    check = false;
                }
            }
        }

        if (check) {
            System.out.println("Все подзадачи для эпика id:" + epicId + " удалены!");
            epicStatusCalculate(epicId);
        }
    }

    public void updateTask(Task updatedTask) {
        taskList.put(updatedTask.getTaskId(), updatedTask);
    }

    public void updateEpic(Epic updatedEpic) {
        epicList.put(updatedEpic.getEpicId(), updatedEpic);
    }

    public void updateSubTask(SubTask updatedSubTask) {
       subTaskList.put(updatedSubTask.getSubtaskId(), updatedSubTask);
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
        subTaskList.remove(subTaskId);
        System.out.println("Подзадача ID:" + subTaskId + " удалена.");
        System.out.println("*".repeat(25));
        epicStatusCalculate(getSubTaskById(subTaskId).getEpicId());
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

    public void changeStatus(int subTaskId, TaskStatus status) {
        System.out.println("subTaskId:" + subTaskId + " Status: " + subTaskList.get(subTaskId).subtaskStatus);
        subTaskList.get(subTaskId).subtaskStatus = status;
        System.out.println("subTaskId:" + subTaskId + " Status: " + subTaskList.get(subTaskId).subtaskStatus);
        epicStatusCalculate(subTaskList.get(subTaskId).epicId);
        System.out.println("EpicId:" + subTaskList.get(subTaskId).epicId + " Status: " + epicList.get(subTaskList.get(subTaskId).epicId).epicStatus);
    }
}
