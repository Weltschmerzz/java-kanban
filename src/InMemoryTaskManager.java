package ru.yandex.practicum.TaskTracker.src;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int id = 0;
    private final Map<Integer, Task> taskList;
    private final Map<Integer, Epic> epicList;
    private final Map<Integer, SubTask> subTaskList;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    public InMemoryTaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subTaskList = new HashMap<>();
    }

    @Override
    public void createTask(Task newTask) {
        int newTaskid = getTaskid();
        newTask.setId(newTaskid);
        taskList.put(newTaskid, newTask);
        System.out.println("Задачи с ID:" + newTaskid + " успешно создана!");
    }

    @Override
    public void createEpic(Epic newEpic) {
        int newTaskid = getTaskid();
        newEpic.setId(newTaskid);
        epicList.put(newTaskid, newEpic);
        System.out.println("Эпик с ID:" + newTaskid + " успешно создан!");

    }

    @Override
    public void createSubTask(SubTask newSubTask) {
        if (Objects.equals(newSubTask.getId(), newSubTask.getEpicId())) {
            System.out.println("Эпик не может быть собственной подзадачей");
            return;
        }
        int newSubTaskid = getTaskid();
        newSubTask.setId(newSubTaskid);
        subTaskList.put(newSubTaskid, newSubTask);
        epicList.get(newSubTask.getEpicId()).addSubTaskId(newSubTaskid);
        epicStatusCalculate(newSubTask.getEpicId());
        System.out.println("Подзадача с ID:" + newSubTaskid + "успешно создана!");
    }

    @Override
    public List<Task> getTaskList() {
        if (taskList.isEmpty()) {
            System.out.println("Список задач пуст!");
            System.out.println("*".repeat(25));
        }
        return new ArrayList<>(taskList.values());
    }

    @Override
    public List<Epic> getEpicList() {
        if (epicList.isEmpty()) {
            System.out.println("Список эпиков пуст!");
            System.out.println("*".repeat(25));
        }
        return new ArrayList<>(epicList.values());
    }

    @Override
    public List<SubTask> getSubTaskList() {
        if (subTaskList.isEmpty()) {
            System.out.println("Список подзадач пуст!");
            System.out.println("*".repeat(25));
        }
        return new ArrayList<>(subTaskList.values());
    }

    @Override
    public List<SubTask> getSubTaskListByEpic(int epicId) {
        boolean hasEpicSubTask = false;
        List<SubTask> subTaskForOneEpic = new ArrayList<>();

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

    @Override
    public void clearTaskList() {
        taskList.clear();
        System.out.println("Все задачи удалены!");
        System.out.println("*".repeat(25));
    }

    @Override
    public void clearEpicList() {
        epicList.clear();
        System.out.println("Все Эпики удалены!");
        subTaskList.clear();
        System.out.println("Все Подзадачи удалены!");
        System.out.println("*".repeat(25));
    }

    @Override
    public void clearSubTaskList() {
        subTaskList.clear();
        System.out.println("Все подзадачи удалены!");

        for (Epic epic : epicList.values()) {
            epicStatusCalculate(epic.getId());
        }
    }

    @Override
    public void clearSubTaskListForEpic(int epicId) {
        List<Integer> removeCandidate = new ArrayList<>();
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

    @Override
    public void updateTask(Task updatedTask) {
        taskList.put(updatedTask.getId(), updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        updatedEpic.setSubTaskIds(epicList.get(updatedEpic.getId()).getSubTaskIds());
        epicList.put(updatedEpic.getId(), updatedEpic);
        epicStatusCalculate(updatedEpic.getId());
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        subTaskList.put(updatedSubTask.getId(), updatedSubTask);
        epicStatusCalculate(updatedSubTask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        taskList.remove(id);
        System.out.println("Задача ID:" + id + " удалена.");
        System.out.println("*".repeat(25));
    }

    @Override
    public void deleteEpic(int epicId) {
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);
        System.out.println("Эпик ID:" + epicId + " удален.");
        System.out.println("*".repeat(25));
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        int epicId = subTaskList.get(subTaskId).getEpicId();
        subTaskList.remove(subTaskId);
        epicList.get(epicId).removeSubTaskId(subTaskId);
        System.out.println("Подзадача ID:" + subTaskId + " удалена.");
        System.out.println("*".repeat(25));
        epicStatusCalculate(epicId);
    }

    @Override
    public Task getTaskById(int taskId) {
        historyManager.add(taskList.get(taskId));
        return taskList.get(taskId);
    }

    @Override
    public Epic getEpicById(int epicId) {
        historyManager.add(epicList.get(epicId));
        return epicList.get(epicId);
    }

    @Override
    public SubTask getSubTaskById(int subTaskId) {
        historyManager.add(subTaskList.get(subTaskId));
        return subTaskList.get(subTaskId);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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
