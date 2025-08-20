package ru.yandex.practicum.TaskTracker.test;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.TaskTracker.src.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    public void shouldManagerGetCorrectObjectInMemoryTaskManager() {
        assertNotNull(tm);
        assertInstanceOf(InMemoryTaskManager.class, tm);
    }

    @Test
    void shouldPrioritizedSortsAndSkipsEpicsAndNullStart() {
        InMemoryTaskManager mgr = new InMemoryTaskManager();
        LocalDateTime b = base;

        Task nullStart = new Task("null", "d", TaskStatus.NEW, null, Duration.ofMinutes(10));
        mgr.createTask(nullStart);

        Epic epic = new Epic("E", "d");
        mgr.createEpic(epic);

        SubTask s1 = new SubTask(epic, "S1", "d", TaskStatus.NEW, b, Duration.ofMinutes(5));
        mgr.createSubTask(s1);

        Task t1 = new Task("T1", "d", TaskStatus.NEW, b.plusHours(1), Duration.ofMinutes(10));
        mgr.createTask(t1);

        List<Task> pr = mgr.getPrioritizedTasks();
        assertEquals(2, pr.size());
        assertEquals(s1.getId(), pr.get(0).getId());
        assertEquals(t1.getId(), pr.get(1).getId());
    }

    @Test
    void shouldPrioritizedReordersOnUpdate() {
        InMemoryTaskManager mgr = new InMemoryTaskManager();
        LocalDateTime b = base;

        Task t = new Task("T", "d", TaskStatus.NEW, b.plusHours(2), Duration.ofMinutes(10));
        mgr.createTask(t);

        Task tUpdated = new Task("T", "d", TaskStatus.NEW, b, Duration.ofMinutes(10));
        tUpdated.setId(t.getId());
        mgr.updateTask(tUpdated);

        List<Task> pr = mgr.getPrioritizedTasks();
        assertEquals(1, pr.size());
        assertEquals(b, pr.getFirst().getStartTime());

        Task tNoTime = new Task("T", "d", TaskStatus.NEW, null, Duration.ofMinutes(10));
        tNoTime.setId(t.getId());
        mgr.updateTask(tNoTime);

        assertTrue(mgr.getPrioritizedTasks().isEmpty());
    }

    @Test
    void shouldPrioritizedUpdatesOnDeleteAndClears() {
        InMemoryTaskManager mgr = new InMemoryTaskManager();
        LocalDateTime b = base;

        Epic epic = new Epic("E", "d");
        mgr.createEpic(epic);

        Task t = new Task("T", "d", TaskStatus.NEW, b.plusHours(1), Duration.ofMinutes(10));
        mgr.createTask(t);
        SubTask s = new SubTask(epic, "S", "d", TaskStatus.NEW, b, Duration.ofMinutes(5));
        mgr.createSubTask(s);

        assertEquals(2, mgr.getPrioritizedTasks().size());

        mgr.deleteTask(t.getId());
        assertEquals(1, mgr.getPrioritizedTasks().size());

        mgr.clearSubTaskList();
        assertTrue(mgr.getPrioritizedTasks().isEmpty());
    }
}
