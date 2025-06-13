package ru.yandex.practicum.TaskTracker.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TaskManager {
    public static int id = 0;
    HashMap<Integer, Task> taskList;
    HashMap<Integer, Epic> epicList;
    HashMap<Integer, SubTask> subTaskList;


    TaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subTaskList = new HashMap<>();
    }

    public static int getTaskid() {
        id++;
        return id;
    }

    public void createTask(String name, String desc, TaskStatus status) {
        int newTaskid = getTaskid();
        taskList.put(newTaskid, new Task(newTaskid, name, desc, status));
        if (taskList.containsKey(newTaskid)) {
            System.out.println("Задачи с ID:" + newTaskid + " успешно создана!");
        } else {
            printAlert();
        }
    }


    public void createEpic(String name, String desc) {
        int newTaskid = getTaskid();
        epicList.put(newTaskid, new Epic(newTaskid, name, desc, TaskStatus.NEW));
        if (epicList.containsKey(newTaskid)) {
            System.out.println("Эпик с ID: " + newTaskid + " успешно создан!");
        } else {
            printAlert();
        }
    }

    public boolean checkTaskId(int taskId) {
        boolean result = true;

        if (!taskList.containsKey(taskId)) {
            System.out.println("Задача с ID:" + taskId + " не найден.");
            System.out.println("*".repeat(25));
            result = false;
        }
        return result;
    }

    public boolean checkEpicId(int epicId) {
        boolean result = false;

        if (!epicList.containsKey(epicId)) {
            System.out.println("Эпик с ID:" + epicId + " не найден.");
            System.out.println("*".repeat(25));
            result = true;
        }
        return result;
    }

    public boolean checkSubTaskId(int subTaskid) {
        boolean result = true;

        if (!subTaskList.containsKey(subTaskid)) {
            System.out.println("Подзадача с ID:" + subTaskid + " не найдена.");
            System.out.println("*".repeat(25));
            result = false;
        }
        return result;
    }


    public boolean isEpicListEmpty() {
        boolean result = false;

        if (epicList.isEmpty()) {
            System.out.println("Нет созданных эпиков. Сначала создайте эпик!");
            System.out.println("*".repeat(25));
            result = true;
        }
        return result;
    }

    public boolean isSubTaskListEmpty() {
        boolean result = false;

        if (subTaskList.isEmpty()) {
            System.out.println("Нет созданных подзадачи. Сначала создайте подзадачу!");
            System.out.println("*".repeat(25));
            result = true;
        }
        return result;
    }


    public void createSubTask(int epicId, String name, String desc, TaskStatus status) {
        int newSubTaskid = getTaskid();
        subTaskList.put(newSubTaskid, new SubTask(epicList.get(epicId), newSubTaskid, name, desc, status));
        if (subTaskList.containsKey(newSubTaskid)) {
            System.out.println("Подзадача c ID:" + newSubTaskid + " для с Эпика ID:" + epicId + " успешно создана!");
            epicStatusCalculate(epicId);
        } else {
            printAlert();
        }
    }

    public void epicStatusCalculate(int epicId) {
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


    public void getTaskList() { //Получить список задач
        if (taskList.isEmpty()) {
            System.out.println("Список задач пуст!");
            System.out.println("*".repeat(25));
        }
        for (Task task : taskList.values()) {
            System.out.println(task.toString());
        }
    }

    public void getEpicList() {
        if (epicList.isEmpty()) {
            System.out.println("Список эпиков пуст!");
            System.out.println("*".repeat(25));
        }
        for (Epic epic : epicList.values()) {
            System.out.println(epic.toString());
        }
    }

    public void getSubTaskList() {
        if (subTaskList.isEmpty()) {
            System.out.println("Список подзадач пуст!");
            System.out.println("*".repeat(25));
        }
        for (SubTask sTask : subTaskList.values()) {
            System.out.println(sTask.toString());
        }
    }

    public void getSubTaskListByEpic(int epicId) {
        boolean hasEpicSubTask = false;

        for (SubTask sTask : subTaskList.values()) {
            if (sTask.epicId == epicId) {
                System.out.println(sTask);
                hasEpicSubTask = true;
            }
        }

        if (!hasEpicSubTask) {
            System.out.println("У данного эпика нет подзадач!");
            System.out.println("*".repeat(25));
        }
    }

    public void clearTaskList() {  //Удалить все задачи из списка
        taskList.clear();
        if (taskList.isEmpty()) {
            System.out.println("Все задачи удалены!");
            System.out.println("*".repeat(25));
        } else {
            printAlert();
        }
    }

    public void clearEpicList() {
        epicList.clear();
        if (epicList.isEmpty()) {
            System.out.println("Все Эпики удалены!");
        } else {
            printAlert();
        }

        subTaskList.clear();
        if (subTaskList.isEmpty()) {
            System.out.println("Все Подзадачи удалены!");
            System.out.println("*".repeat(25));
        } else {
            printAlert();
        }
    }

    public void clearSubTaskList() {
        subTaskList.clear();
        if (subTaskList.isEmpty()) {
            System.out.println("Все подзадачи удалены!");
        }
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
        } else {
            printAlert();
        }
    }

    public void updateTask(int id, String name, String desc, TaskStatus status) {
        taskList.put(id, new Task(id, name, desc, status));

        if (taskList.get(id).taskName == name && taskList.get(id).taskDescription == desc) {
            System.out.println("Данные для задачи ID:" + id + " обновлены.");
            System.out.println("*".repeat(25));
        } else {
            printAlert();
        }
    }

    public void updateEpic(int id, String name, String desc) {
        epicList.put(id, new Epic(id, name, desc, TaskStatus.NEW));

        if (epicList.get(id).epicName == name && epicList.get(id).epicDescription == desc) {
            System.out.println("Данные для эпика ID:" + id + " обновлены.");
            System.out.println("*".repeat(25));
        } else {
            printAlert();
        }
    }

    public void updateSubTask(int id, String name, String desc, TaskStatus status) {
        int epicId = subTaskList.get(id).epicId;
        subTaskList.put(id, new SubTask(epicList.get(epicId), id, name, desc, status));

        if (subTaskList.get(id).subtaskName == name && subTaskList.get(id).subtaskDescription == desc) {
            System.out.println("Данные для подзадачи ID:" + id + " обновлены.");
            System.out.println("*".repeat(25));
            epicStatusCalculate(epicId);
        } else {
            printAlert();
        }
    }

    public void deleteTask(int id) {
        taskList.remove(id);
        if (taskList.containsKey(id)) {
            printAlert();
        } else {
            System.out.println("Задача ID:" + id + " удалена.");
            System.out.println("*".repeat(25));
        }
    }

    public void deleteEpic(int epicId) {
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);

        if (epicList.containsKey(epicId)) {
            printAlert();
        } else {
            System.out.println("Эпик ID:" + epicId + " удален.");
            System.out.println("*".repeat(25));
        }
    }

    public void deleteSubTask(int subTaskId) {
        subTaskList.remove(subTaskId);

        if (subTaskList.containsValue(subTaskId)) {
            printAlert();
        } else {
            System.out.println("Подзадача ID:" + subTaskId + " удалена.");
            System.out.println("*".repeat(25));
        }
    }

    public void getTaskById(Scanner sc) {
        System.out.print("Введите ID задачи:");
        int id = sc.nextInt();
        sc.nextLine();
        if (taskList.containsKey(id)) {
            System.out.println(taskList.get(id).toString());
        } else {
            System.out.println("Задача с ID:" + id + " не найдена.");
        }
    }

    public void getEpicById(Scanner sc) {
        System.out.print("Введите ID эпика: ");
        int id = sc.nextInt();
        sc.nextLine();
        if (epicList.containsKey(id)) {
            System.out.println(epicList.get(id).toString());
        } else {
            System.out.println("Эпик с ID: " + id + " не найдена.");
        }
    }

    public void getSubTaskById(Scanner sc) {
        System.out.print("Введите ID подзадачи: ");
        int id = sc.nextInt();
        sc.nextLine();
        if (subTaskList.containsKey(id)) {
            System.out.println(subTaskList.get(id).toString());
        } else {
            System.out.println("Эпик с ID: " + id + " не найдена.");
        }
    }

    private void printAlert() {
        System.out.println("Что-то пошло не так...");
        System.out.println("*".repeat(25));
    }

    public void changeStatus(int subTaskId, TaskStatus status) {
        System.out.println("subTaskId:" + subTaskId + " Status: " + subTaskList.get(subTaskId).subtaskStatus);
        subTaskList.get(subTaskId).subtaskStatus = status;
        System.out.println("subTaskId:" + subTaskId + " Status: " + subTaskList.get(subTaskId).subtaskStatus);
        epicStatusCalculate(subTaskList.get(subTaskId).epicId);
        System.out.println("EpicId:" + subTaskList.get(subTaskId).epicId + " Status: " + epicList.get(subTaskList.get(subTaskId).epicId).epicStatus);
    }
}
