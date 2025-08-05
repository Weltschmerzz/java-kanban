package ru.yandex.practicum.TaskTracker.src;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Managers {

    public static TaskManager getDefault(String environment) {
        Path prod = Paths.get("storage/prod", "storage.csv");
        Path test = Paths.get("storage/test", "test_storage.csv");

        switch (environment) {
            case "prod" -> {
                return FileBackedTaskManager.loadFromFile(prod);
            }
            case "test" -> {
                return FileBackedTaskManager.loadFromFile(test);
            }
            default -> {
                System.out.println("Задано некорректное окружение: " + environment);
                return new FileBackedTaskManager(test);
            }
        }
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
