package ru.yandex.practicum.TaskTracker.src;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int id = 0;
    private final Map<Integer, Task> taskList;
    private final Map<Integer, Epic> epicList;
    private final Map<Integer, SubTask> subTaskList;
    private final Set<Task> prioritizedSet;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    public InMemoryTaskManager() {
        taskList = new HashMap<>();
        epicList = new HashMap<>();
        subTaskList = new HashMap<>();
        prioritizedSet = new TreeSet<>(PRIORITY_COMPARATOR);
    }

    @Override
    public void createTask(Task newTask) {
        int newTaskid = getTaskid();
        newTask.setId(newTaskid);
        if (hasTaskAnyIntersects(newTask)) {
            throw new IllegalStateException("Конфликт: пересечений по времени с существующей задачей!");
        }
        taskList.put(newTaskid, newTask);
        prioritizeTaskSet(newTask);
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

        if (hasTaskAnyIntersects(newSubTask)) {
            throw new IllegalStateException("Конфликт: пересечений по времени с существующей задачей!");
        }

        subTaskList.put(newSubTaskid, newSubTask);
        prioritizeTaskSet(newSubTask);
        epicList.get(newSubTask.getEpicId()).addSubTaskId(newSubTaskid);
        epicCalculate(newSubTask.getEpicId());
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
        List<SubTask> subTaskListByEpic = epicList.get(epicId).getSubTaskIds().stream()
                .map(subTaskList::get)
                .filter(Objects::nonNull)
                .toList();
        if (subTaskListByEpic.isEmpty()) {
            System.out.println("У данного эпика нет подзадач!");
            System.out.println("*".repeat(25));
        }
        return subTaskListByEpic;
    }

    @Override
    public void clearTaskList() {
        taskList.values()
                .forEach(task -> {
                    removeFromPrioritized(task);
                    historyManager.remove(task.getId());
                });
        taskList.clear();
        System.out.println("Все задачи удалены!");
        System.out.println("*".repeat(25));

    }

    @Override
    public void clearEpicList() {
        clearSubTaskList();
        System.out.println("Все Подзадачи удалены!");
        epicList.values()
                .forEach(epic -> {
                    removeFromPrioritized(epic);
                    historyManager.remove(epic.getId());
                });
        epicList.clear();
        System.out.println("Все Эпики удалены!");
        System.out.println("*".repeat(25));
    }

    @Override
    public void clearSubTaskList() {
        subTaskList.values()
                .forEach(sub -> {
                    removeFromPrioritized(sub);
                    historyManager.remove(sub.getId());
                });
        subTaskList.clear();
        System.out.println("Все подзадачи удалены!");

        epicList.values()
                .forEach(epic -> {
                    epic.getSubTaskIds().clear();
                    epicCalculate(epic.getId());
                });
    }

    @Override
    public void clearSubTaskListForEpic(int epicId) {
        subTaskList.values()
                .removeIf(subTask -> {
                    boolean match = subTask.getEpicId() == epicId;
                    if (match) removeFromPrioritized(subTask);
                    if (match) historyManager.remove(subTask.getId());
                    return match;
                });
        epicList.get(epicId).getSubTaskIds().clear();
        System.out.println("Все подзадачи для эпика id:" + epicId + " удалены!");
        epicCalculate(epicId);
    }

    @Override
    public void updateTask(Task updatedTask) {
        Task oldTask = taskList.get(updatedTask.getId());
        removeFromPrioritized(oldTask);

        if (hasTaskAnyIntersects(updatedTask)) {
            prioritizeTaskSet(oldTask);
            throw new IllegalStateException("Конфликт: пересечений по времени с существующей задачей!");
        }

        taskList.put(updatedTask.getId(), updatedTask);

        prioritizeTaskSet(updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        updatedEpic.setSubTaskIds(epicList.get(updatedEpic.getId()).getSubTaskIds());
        epicList.put(updatedEpic.getId(), updatedEpic);
        epicCalculate(updatedEpic.getId());
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        SubTask oldSubTask = subTaskList.get(updatedSubTask.getId());
        removeFromPrioritized(oldSubTask);

        if (hasTaskAnyIntersects(updatedSubTask)) {
            prioritizeTaskSet(oldSubTask);
            throw new IllegalStateException("Конфликт: пересечений по времени с существующей задачей!");
        }

        subTaskList.put(updatedSubTask.getId(), updatedSubTask);

        prioritizeTaskSet(updatedSubTask);
        epicCalculate(updatedSubTask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        removeFromPrioritized(taskList.get(id));
        taskList.remove(id);
        historyManager.remove(id);
        System.out.println("Задача ID:" + id + " удалена.");
        System.out.println("*".repeat(25));
    }

    @Override
    public void deleteEpic(int epicId) {
        clearSubTaskListForEpic(epicId);
        removeFromPrioritized(getEpicById(epicId));
        historyManager.remove(epicId);
        epicList.remove(epicId);
        System.out.println("Эпик ID:" + epicId + " удален.");
        System.out.println("*".repeat(25));
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        SubTask deletedSubTask = subTaskList.get(subTaskId);
        if (deletedSubTask != null) {
            removeFromPrioritized(deletedSubTask);
            historyManager.remove(subTaskId);
            int epicId = deletedSubTask.getEpicId();
            subTaskList.remove(subTaskId);
            epicList.get(epicId).removeSubTaskId(subTaskId);
            System.out.println("Подзадача ID:" + subTaskId + " удалена.");
            System.out.println("*".repeat(25));
            epicCalculate(epicId);
        }
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
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedSet);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public boolean hasTaskAnyIntersects(Task candidate) {
        if (candidate == null || candidate.getStartTime() == null || candidate.getEndTime() == null)
            return false;
        return prioritizedSet.stream()
                .filter(taskFromSet -> taskFromSet.getId() != candidate.getId())
                .anyMatch(taskFromSet -> isTwoTaskOverlaps(candidate, taskFromSet));
    }

    protected void updateIdCounterAfterLoad() {
        int maxId = 0;
        for (Task task : getTaskMap().values()) {
            maxId = Math.max(maxId, task.getId());
        }
        for (Epic epic : getEpicMap().values()) {
            maxId = Math.max(maxId, epic.getId());
        }
        for (SubTask subTask : getSubTaskMap().values()) {
            maxId = Math.max(maxId, subTask.getId());
        }
        this.id = maxId;
    }

    protected Map<Integer, Task> getTaskMap() {
        return taskList;
    }

    protected Map<Integer, Epic> getEpicMap() {
        return epicList;
    }

    protected Map<Integer, SubTask> getSubTaskMap() {
        return subTaskList;
    }

    protected void epicCalculate(int epicId) {
        List<SubTask> subs = getSubTaskListByEpic(epicId);
        Epic epic = epicList.get(epicId);

        if (subs.isEmpty()) {
            epic.setCalculatedTime(null, null, Duration.ZERO);
            epicStatusCalculate(epicId);
            return;
        }

        Duration totalSum = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        for (SubTask subTask : subs) {
            Duration duration = subTask.getDuration();
            if (duration != null) {
                totalSum = totalSum.plus(duration);
            }

            LocalDateTime start = subTask.getStartTime();
            LocalDateTime end = subTask.getEndTime();

            if (start != null && (minStart == null || start.isBefore(minStart))) {
                minStart = start;
            }
            if (end != null && (maxEnd == null || end.isAfter(maxEnd))) {
                maxEnd = end;
            }
        }

        epic.setCalculatedTime(minStart, maxEnd, totalSum);
        epicStatusCalculate(epicId);
    }

    protected void prioritizeTaskSet(Task task) {
        if (task == null) return;
        if (task instanceof Epic) return;
        if (task.getStartTime() == null) return;
        prioritizedSet.add(task);
    }

    protected void removeFromPrioritized(Task task) {
        if (task == null) return;
        if (task instanceof Epic) return;
        if (task.getStartTime() == null) return;
        prioritizedSet.remove(task);
    }

    protected void remakePrioritizedTaskSet() {
        prioritizedSet.clear();
        for (Task task : taskList.values()) {
            prioritizeTaskSet(task);
        }
        for (SubTask subTask : subTaskList.values()) {
            prioritizeTaskSet(subTask);
        }
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

    private static final Comparator<Task> PRIORITY_COMPARATOR = new Comparator<>() {
        @Override
        public int compare(Task a, Task b) {
            if (a == b) return 0;

            LocalDateTime sta = a.getStartTime();
            LocalDateTime stb = b.getStartTime();

            if (sta == null && stb == null) {
                return Integer.compare(a.getId(), b.getId());
            }
            if (sta == null) return 1;   // a после b
            if (stb == null) return -1;  // a перед b

            int byTime = sta.compareTo(stb);
            if (byTime != 0) return byTime;

            return Integer.compare(a.getId(), b.getId());
        }
    };

    private boolean isTwoTaskOverlaps(Task a, Task b) {
        if (a == null || b == null) return false;
        if (a.getId() == b.getId()) return false;

        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();

        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        if (aStart == null || bStart == null || aEnd == null || bEnd == null) return false;

        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }


}
