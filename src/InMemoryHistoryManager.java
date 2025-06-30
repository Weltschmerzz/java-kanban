package ru.yandex.practicum.TaskTracker.src;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> historyList = new ArrayList<>();
    private final int HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if(!historyList.isEmpty() && historyList.getLast() == task) {
             return;
        }
        if (historyList.size() == HISTORY_SIZE) {
            historyList.removeFirst();
        }
        historyList.add(task);
    }

    @Override
    public List<Task> getHistory(){
        if(historyList.isEmpty()) {
            System.out.println("История просмотров пуста.");
            return historyList;
        }
        return historyList;
    }
}
