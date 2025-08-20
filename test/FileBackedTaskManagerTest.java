package ru.yandex.practicum.TaskTracker.test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.TaskTracker.src.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<TaskManager> {

    private Path testFile;

    @Override
    protected void beforeEachImpl() {
        testFile = Paths.get("storage/test", "test_storage.csv");
        try {
            Files.createDirectories(testFile.getParent());
            if (Files.exists(testFile)) {
                Files.delete(testFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected TaskManager createManager() {
        return Managers.getDefault("test");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testFile)) Files.delete(testFile);
    }

    @Test
    void shouldCreateNewFileIfNotExists() {
        assertTrue(Files.exists(testFile));
    }

    @Test
    void shouldLoadTasksFromFileCorrectly() {
        TaskManager manager = Managers.getDefault("test");
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, LocalDateTime.now().plusDays(1));
        manager.createTask(new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW, startTime, duration));
        Task task1 = manager.getTaskList().getFirst();

        TaskManager anotherManager = Managers.getDefault("test");
        Task task2 = anotherManager.getTaskList().getFirst();

        assertEquals(task1.toString(), task2.toString());
    }

    @Test
    void shouldRecalculateEpicTimeAfterReload() {
        // исходный менеджер пишет в файл
        TaskManager src = Managers.getDefault("test");
        LocalDateTime b = LocalDateTime.of(2025, 1, 1, 18, 0);

        Epic epic = new Epic("E", "D");
        src.createEpic(epic);
        src.createSubTask(new SubTask(epic, "S1", "D1", TaskStatus.NEW, b, Duration.ofMinutes(30)));
        src.createSubTask(new SubTask(epic, "S2", "D2", TaskStatus.DONE, b.plusHours(1), Duration.ofMinutes(20)));

        // загрузка из файла
        TaskManager dst = Managers.getDefault("test");
        Epic e = dst.getEpicById(epic.getId());

        assertEquals(b, e.getStartTime());
        assertEquals(b.plusHours(1).plusMinutes(20), e.getEndTime());
        assertEquals(Duration.ofMinutes(50), e.getDuration());
        assertEquals(TaskStatus.IN_PROGRESS, e.getStatus());
    }
}
