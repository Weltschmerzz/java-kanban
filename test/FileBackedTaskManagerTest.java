package ru.yandex.practicum.TaskTracker.test;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.TaskTracker.src.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private static TaskManager manager;
    private static TaskManager anotherManager;

    @BeforeEach
    public void beforeEach() throws IOException {
        Path testFile = Paths.get("storage/test", "test_storage.csv");
        if (Files.exists(testFile)) {
            Files.delete(testFile);
        }
    }

    @Test
    void shouldCreateNewFileIfNotExists() {
        manager = Managers.getDefault("test");
        assertTrue(Files.exists(Paths.get("storage/test", "test_storage.csv")));
    }

    @Test
    void shouldLoadTasksFromFileCorrectly() {
        manager = Managers.getDefault("test");
        manager.createTask(new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW));
        Task task1 = manager.getTaskList().getFirst();

        anotherManager = Managers.getDefault("test");
        Task task2 = anotherManager.getTaskList().getFirst();

        assertEquals(task1.toString(), task2.toString());
    }
}
