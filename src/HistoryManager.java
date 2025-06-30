package ru.yandex.practicum.TaskTracker.src;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}
