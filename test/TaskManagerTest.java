package ru.yandex.practicum.TaskTracker.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.TaskTracker.src.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T tm;
    protected final LocalDateTime base = LocalDateTime.of(2025, 1, 1, 9, 0);

    protected abstract T createManager();

    protected void beforeEachImpl() {
    }

    @BeforeEach
    void setUp() {
        beforeEachImpl();
        tm = createManager();
        addTestTasks();
    }

    //Убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных
    @Test
    public void shouldStoreOriginalTaskDataInHistory() {
        String originalName = tm.getTaskById(4).getName(); // добавляем в историю
        assertEquals(1, tm.getHistory().size());

        Task newTask = new Task("NewTask", "NewDesc", TaskStatus.DONE, base.plusHours(9), Duration.ofMinutes(30));
        newTask.setId(4);
        tm.updateTask(newTask);

        assertEquals(originalName, tm.getHistory().getFirst().getName()); // в истории осталась старая версия
    }

    //Проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    public void shouldNotConflictManualIdAndAutomaticId() {
        // Сдвинул времена, чтобы не пересекалось с сидом
        Task t16 = new Task("N", "D", TaskStatus.NEW, base.plusHours(9), Duration.ofMinutes(30));
        t16.setId(16);
        tm.createTask(t16);
        assertEquals(t16, tm.getTaskById(16));

        Task t17 = new Task("Name", "Desc", TaskStatus.IN_PROGRESS, base.plusHours(10), Duration.ofMinutes(30));
        tm.createTask(t17);
        assertEquals(t17, tm.getTaskById(17));
    }

    //Проверьте, что наследники класса Task равны друг другу, если равен их id
    @Test
    public void shouldTasksEqualsIfEqualItsIDs() {
        Task task1 = new Task("Задача 2", "Описание задачи 2", TaskStatus.IN_PROGRESS, base.plusHours(0), Duration.ofMinutes(30));
        task1.setId(3);
        Task task2 = new Task("Другая задача", "Другое описание", TaskStatus.DONE, base.plusHours(0), Duration.ofMinutes(30));
        task2.setId(3);
        assertEquals(task1, task2);

        Epic epic1 = new Epic("Эпик 2", "Описание эпика 2");
        epic1.setId(7);
        Epic epic2 = new Epic("Другой эпик", "Другое описание");
        epic2.setId(7);
        assertEquals(epic1, epic2);

        Epic epicForSubTask = tm.getEpicById(6); // существующий эпик
        SubTask subTask1 = new SubTask(epicForSubTask, "Подзадача 4", "Описание подзадачи 4", TaskStatus.NEW, base.plusHours(4), Duration.ofMinutes(30));
        subTask1.setId(14);
        SubTask subTask2 = new SubTask(epicForSubTask, "Другая подзадача", "Другое описание", TaskStatus.DONE, base.plusHours(5), Duration.ofMinutes(60));
        subTask2.setId(14);
        assertEquals(subTask1, subTask2);
    }

    //Проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи
    //Проверьте, что объект Subtask нельзя сделать своим же эпиком
    @Test
    public void shouldRejectAddEpicInItsSubTaskList() {
        Epic epic = tm.getEpicById(7);
        SubTask invalid = new SubTask(epic, "NewTestSubTask", "NewTestSubTaskDescription",
                TaskStatus.NEW, base.plusHours(6), Duration.ofMinutes(120));
        invalid.setId(7); // совпадает с epicId
        tm.createSubTask(invalid);
        assertNull(tm.getSubTaskById(7));
        assertEquals(5, tm.getSubTaskList().size());
    }

    @Test
    public void subTaskListInEpicCalculatedCorrectly() {
        assertEquals(2, tm.getEpicById(7).getSubTaskIds().size());
        int subTaskIdToDelete = tm.getEpicById(7).getSubTaskIds().getFirst();
        tm.deleteSubTask(subTaskIdToDelete);
        assertFalse(tm.getEpicById(7).getSubTaskIds().contains(subTaskIdToDelete));
    }

    @Test
    public void shouldGetHistoryReturnCorrectList() {
        tm.getTaskById(4);
        tm.getTaskById(5);
        tm.getEpicById(9);
        tm.getEpicById(10);
        tm.getSubTaskById(14);
        tm.getSubTaskById(15);

        assertNotNull(tm.getHistory());
        assertEquals(6, tm.getHistory().size());
        assertEquals("Задача 4", tm.getHistory().getFirst().getName());
        assertEquals("Описание эпика 4", tm.getHistory().get(2).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, tm.getHistory().getLast().getStatus());

        tm.getTaskById(5);
        assertEquals(5, tm.getHistory().getLast().getId());
        tm.getEpicById(10);
        assertEquals(10, tm.getHistory().getLast().getId());
        tm.getSubTaskById(15);
        assertEquals(15, tm.getHistory().getLast().getId());
        assertEquals(6, tm.getHistory().size());
    }

    @Test
    public void shouldAddTasksToHistoryListCorrectly() {
        tm.getTaskById(66);
        assertTrue(tm.getHistory().isEmpty());

        tm.getSubTaskById(13);
        tm.getTaskById(66);
        tm.getSubTaskById(13);
        assertEquals(1, tm.getHistory().size());

        tm.getTaskById(2);
        tm.getSubTaskById(13);
        tm.getEpicById(7);
        tm.getSubTaskById(13);
        assertEquals(3, tm.getHistory().size());
    }

    @Test
    public void historyRemovesItemsOnEntityDelete_start_middle_end() {
        tm.getTaskById(4);
        tm.getTaskById(5);
        tm.getEpicById(7);
        tm.getEpicById(10);
        tm.getSubTaskById(14);
        tm.getSubTaskById(15);

        assertEquals(6, tm.getHistory().size());
        assertEquals(4, tm.getHistory().getFirst().getId());
        assertEquals(15, tm.getHistory().getLast().getId());

        //Удаление из начала
        tm.deleteTask(4);
        assertEquals(5, tm.getHistory().size());
        assertEquals(5, tm.getHistory().getFirst().getId());
        assertEquals(15, tm.getHistory().getLast().getId());

        //Удаление из середины
        tm.deleteEpic(10);
        assertEquals(4, tm.getHistory().size());
        assertEquals(5, tm.getHistory().get(0).getId());
        assertEquals(7, tm.getHistory().get(1).getId());
        assertEquals(14, tm.getHistory().get(2).getId());
        assertEquals(15, tm.getHistory().get(3).getId());

        //Удаление с конца
        tm.deleteSubTask(15);
        assertEquals(3, tm.getHistory().size());
        assertEquals(14, tm.getHistory().getLast().getId());
    }

    //Создание задач и получение списков
    @Test
    public void shouldCreateAndReturnTaskList() {
        assertEquals(5, tm.getTaskList().size());
        assertEquals("Задача 1", tm.getTaskList().getFirst().getName());
        assertEquals("Описание задачи 1", tm.getTaskList().getFirst().getDescription());
        assertEquals(TaskStatus.NEW, tm.getTaskList().getFirst().getStatus());
    }

    @Test
    public void shouldCreateAndReturnEpicList() {
        assertEquals(5, tm.getEpicList().size());
        assertEquals("Эпик 1", tm.getEpicList().getFirst().getName());
        assertEquals("Описание эпика 1", tm.getEpicList().getFirst().getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, tm.getEpicList().getFirst().getStatus());
    }

    @Test
    public void shouldCreateAndReturnSubTaskList() {
        assertEquals(5, tm.getSubTaskList().size());
        assertEquals("Подзадача 1", tm.getSubTaskList().getFirst().getName());
        assertEquals("Описание подзадачи 1", tm.getSubTaskList().getFirst().getDescription());
        assertEquals(TaskStatus.NEW, tm.getSubTaskList().getFirst().getStatus());
        assertEquals(6, tm.getSubTaskList().getFirst().getEpicId());
    }

    //Получение задач по id
    @Test
    public void shouldReturnTaskById() {
        assertEquals("Задача 2", tm.getTaskById(2).getName());
        assertEquals("Описание задачи 2", tm.getTaskById(2).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, tm.getTaskById(2).getStatus());
    }

    @Test
    public void shouldReturnEpicByIdWithStatusInProgress() {
        assertEquals("Эпик 2", tm.getEpicById(7).getName());
        assertEquals("Описание эпика 2", tm.getEpicById(7).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, tm.getEpicById(7).getStatus());
    }

    @Test
    public void shouldReturnSubTaskListByEpicId() {
        assertEquals(2, tm.getSubTaskListByEpic(6).size());
        assertEquals("Подзадача 1", tm.getSubTaskListByEpic(6).getFirst().getName());
        assertEquals("Описание подзадачи 1", tm.getSubTaskListByEpic(6).getFirst().getDescription());
        assertEquals(TaskStatus.NEW, tm.getSubTaskListByEpic(6).getFirst().getStatus());
    }

    @Test
    public void shouldReturnSubTaskById() {
        assertEquals("Подзадача 4", tm.getSubTaskById(14).getName());
        assertEquals("Описание подзадачи 4", tm.getSubTaskById(14).getDescription());
        assertEquals(TaskStatus.NEW, tm.getSubTaskById(14).getStatus());
    }

    //Обновление задач по ID
    @Test
    public void shouldReturnNewTaskById() {
        // ВРЕМЯ СДВИНУТО, чтобы не пересечься с сидом
        Task updatedTask = new Task("NewTask", "NewTaskDesc", TaskStatus.DONE,
                base.plusHours(9), Duration.ofMinutes(30));
        updatedTask.setId(4);
        tm.updateTask(updatedTask);

        Task task = tm.getTaskById(4);
        assertEquals("NewTask", task.getName());
        assertEquals("NewTaskDesc", task.getDescription());
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    public void shouldReturnNewEpicByIdWithRightStatus() {
        Epic updatedEpic = new Epic("NewEpic", "NewEpicDesc");
        updatedEpic.setId(8);
        tm.updateEpic(updatedEpic);
        assertEquals("NewEpic", tm.getEpicById(8).getName());
        assertEquals("NewEpicDesc", tm.getEpicById(8).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, tm.getEpicById(8).getStatus());
    }

    @Test
    public void shouldReturnNewSubTaskByIdAndChangeEpicStatusToDone() {
        // Сдвинули время подзадачи, чтобы не пересекать чужие
        SubTask updatedSubTask = new SubTask(
                tm.getEpicById(8), "NewSubTask", "NewSubTaskDesc",
                TaskStatus.DONE, base.plusHours(9), Duration.ofMinutes(60)
        );
        updatedSubTask.setId(15);
        tm.updateSubTask(updatedSubTask);

        assertEquals("NewSubTask", tm.getSubTaskById(15).getName());
        assertEquals("NewSubTaskDesc", tm.getSubTaskById(15).getDescription());
        assertEquals(TaskStatus.DONE, tm.getSubTaskById(15).getStatus());
        assertEquals(TaskStatus.DONE, tm.getEpicById(8).getStatus());
    }

    //Удаление задач по ID
    @Test
    public void shouldDeleteTaskById() {
        tm.deleteTask(3);
        assertEquals(4, tm.getTaskList().size());
        assertNull(tm.getTaskById(3));
    }

    @Test
    public void shouldDeleteEpicByIdAndItsSubTasks() {
        tm.deleteEpic(6);
        assertEquals(4, tm.getEpicList().size());
        assertNull(tm.getTaskById(6));
        assertNull(tm.getSubTaskById(11));
        assertNull(tm.getSubTaskById(12));
    }

    @Test
    public void shouldDeleteSubTaskByIdAndChangeEpicStatusToNew() {
        assertEquals(TaskStatus.IN_PROGRESS, tm.getEpicById(8).getStatus());
        tm.deleteSubTask(15);
        assertEquals(4, tm.getSubTaskList().size());
        assertNull(tm.getSubTaskById(15));
        assertEquals(TaskStatus.NEW, tm.getEpicById(8).getStatus());
    }

    //Очистить списки
    @Test
    public void shouldCleanTaskList() {
        tm.clearTaskList();
        assertEquals(0, tm.getTaskList().size());
    }

    @Test
    public void shouldCleanSubListByEpicAndChangeEpicStatusToNew() {
        tm.clearSubTaskListForEpic(7);
        assertEquals(0, tm.getEpicById(7).getSubTaskIds().size());
        assertNull(tm.getSubTaskById(13));
        assertNull(tm.getSubTaskById(14));
        assertEquals(TaskStatus.NEW, tm.getEpicById(7).getStatus());
    }

    @Test
    public void shouldCleanEpicListAndSubTaskList() {
        tm.clearEpicList();
        assertEquals(0, tm.getEpicList().size());
        assertEquals(0, tm.getSubTaskList().size());
    }

    //Посмотреть по ID (доп. проверки)
    @Test
    public void shouldReturnTaskById2() {
        tm.getTaskById(4);
        assertEquals(4, tm.getTaskById(4).getId());
    }

    @Test
    public void shouldReturnEpicById() {
        tm.getEpicById(10);
        assertEquals(10, tm.getEpicById(10).getId());
    }

    @Test
    public void shouldReturnSubTuskById() {
        tm.getSubTaskById(14);
        assertEquals(14, tm.getSubTaskById(14).getId());
    }

    //Проверка пересчета времени
    @Test
    public void shouldCalculateEpicTimeFields() {
        Epic e1 = tm.getEpicById(6);
        assertEquals(base, e1.getStartTime());
        assertEquals(base.plusHours(2).plusMinutes(45), e1.getEndTime());
        assertEquals(Duration.ofMinutes(15 + 45), e1.getDuration());

        Epic e2 = tm.getEpicById(7);
        assertEquals(base.plusHours(4), e2.getStartTime());
        assertEquals(base.plusHours(6), e2.getEndTime());
        assertEquals(Duration.ofMinutes(30 + 60), e2.getDuration());

        Epic e3 = tm.getEpicById(8);
        assertEquals(base.plusHours(6), e3.getStartTime());
        assertEquals(base.plusHours(8), e3.getEndTime());
        assertEquals(Duration.ofMinutes(120), e3.getDuration());
    }

    @Test
    public void taskEndTimeIsStartPlusDuration() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
        Task t = new Task("t", "d", TaskStatus.NEW, start, Duration.ofMinutes(90));
        assertEquals(start.plusMinutes(90), t.getEndTime());
    }

    @Test
    public void epicRecalculatesOnSubtaskCreateUpdateDelete() {
        Epic epic = new Epic("E", "D");
        tm.createEpic(epic);

        // create
        SubTask s = new SubTask(epic, "S", "D", TaskStatus.NEW, base.plusMinutes(30), Duration.ofMinutes(30));
        tm.createSubTask(s);

        Epic e1 = tm.getEpicById(epic.getId());
        assertEquals(base.plusMinutes(30), e1.getStartTime());
        assertEquals(base.plusMinutes(60), e1.getEndTime());
        assertEquals(Duration.ofMinutes(30), e1.getDuration());

        // update
        SubTask upd = new SubTask(epic, "S", "D", TaskStatus.NEW, base.plusHours(1), Duration.ofMinutes(45));
        upd.setId(s.getId());
        tm.updateSubTask(upd);

        Epic e2 = tm.getEpicById(epic.getId());
        assertEquals(base.plusHours(1), e2.getStartTime());
        assertEquals(base.plusHours(1).plusMinutes(45), e2.getEndTime());
        assertEquals(Duration.ofMinutes(45), e2.getDuration());

        // delete
        tm.deleteSubTask(upd.getId());
        Epic e3 = tm.getEpicById(epic.getId());
        assertNull(e3.getStartTime());
        assertNull(e3.getEndTime());
        assertEquals(Duration.ZERO, e3.getDuration());
        assertEquals(TaskStatus.NEW, e3.getStatus());
    }

    //Если все подзадачи NEW - эпик NEW
    @Test
    void shouldEpicNewStatus() {
        Epic epic6 = new Epic("N", "D");
        tm.createEpic(epic6);

        tm.createSubTask(new SubTask(epic6, "s1", "d1", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(30)));
        tm.createSubTask(new SubTask(epic6, "s2", "d2", TaskStatus.NEW, LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        Epic stored = tm.getEpicById(epic6.getId());
        assertEquals(TaskStatus.NEW, stored.getStatus());
    }

    //Если все подзадачи DONE - эпик DONE
    @Test
    void shouldEpicDoneStatus() {
        Epic epic7 = new Epic("N", "D");
        tm.createEpic(epic7);

        tm.createSubTask(new SubTask(epic7, "s1", "d1", TaskStatus.DONE, LocalDateTime.now(), Duration.ofMinutes(30)));
        tm.createSubTask(new SubTask(epic7, "s2", "d2", TaskStatus.DONE, LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        Epic stored = tm.getEpicById(epic7.getId());
        assertEquals(TaskStatus.DONE, stored.getStatus());
    }

    //Если есть хотя бы одна IN_PROGRESS - эпик IN_PROGRESS
    @Test
    void shouldEpicInProgressStatus1() {
        Epic epic8 = new Epic("N", "D");
        tm.createEpic(epic8);

        tm.createSubTask(new SubTask(epic8, "s1", "d1", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(30)));
        tm.createSubTask(new SubTask(epic8, "s2", "d2", TaskStatus.DONE, LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        Epic stored = tm.getEpicById(epic8.getId());
        assertEquals(TaskStatus.IN_PROGRESS, stored.getStatus());
    }

    @Test
    void shouldEpicInProgressStatus2() {
        Epic epic9 = new Epic("N", "D");
        tm.createEpic(epic9);

        tm.createSubTask(new SubTask(epic9, "s1", "d1", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(30)));
        tm.createSubTask(new SubTask(epic9, "s2", "d2", TaskStatus.IN_PROGRESS, LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        Epic stored = tm.getEpicById(epic9.getId());
        assertEquals(TaskStatus.IN_PROGRESS, stored.getStatus());
    }


    private void addTestTasks() {
        LocalDateTime base = this.base;

        LocalDateTime cursor = base.minusHours(5); // 04:00
        Task t1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW, cursor, Duration.ofMinutes(30)); // 04:00–04:30
        tm.createTask(t1);

        cursor = t1.getEndTime().plusMinutes(5); // 04:35
        Task t2 = new Task("Задача 2", "Описание задачи 2", TaskStatus.IN_PROGRESS, cursor, Duration.ofMinutes(45)); // 04:35–05:20
        tm.createTask(t2);

        cursor = t2.getEndTime().plusMinutes(5); // 05:25
        Task t3 = new Task("Задача 3", "Описание задачи 3", TaskStatus.DONE, cursor, Duration.ofMinutes(60)); // 05:25–06:25
        tm.createTask(t3);

        cursor = t3.getEndTime().plusMinutes(5); // 06:30
        Task t4 = new Task("Задача 4", "Описание задачи 4", TaskStatus.NEW, cursor, Duration.ofMinutes(90)); // 06:30–08:00
        tm.createTask(t4);

        // Сделаем так, чтобы последняя задача закончилась до 09:00
        Task t5 = new Task("Задача 5", "Описание задачи 5", TaskStatus.IN_PROGRESS, base.minusMinutes(40), Duration.ofMinutes(30)); // 08:20–08:50
        tm.createTask(t5);

        // 2) Эпики + сабтаски
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        tm.createEpic(epic1);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        tm.createEpic(epic2);
        Epic epic3 = new Epic("Эпик 3", "Описание эпика 3");
        tm.createEpic(epic3);
        Epic epic4 = new Epic("Эпик 4", "Описание эпика 4");
        tm.createEpic(epic4);
        Epic epic5 = new Epic("Эпик 5", "Описание эпика 5");
        tm.createEpic(epic5);

        // epic1: (0h,15m) и (2h,45m) -> start=base, end=base+2:45, duration=60m
        tm.createSubTask(new SubTask(epic1, "Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, base.plusHours(0), Duration.ofMinutes(15)));
        tm.createSubTask(new SubTask(epic1, "Подзадача 2", "Описание подзадачи 2", TaskStatus.IN_PROGRESS, base.plusHours(2), Duration.ofMinutes(45)));

        // epic2: (4h, 30m) и (5h, 60m) -> start=base+4h, end=base+6h, duration=90m
        tm.createSubTask(new SubTask(epic2, "Подзадача 3", "Описание подзадачи 3", TaskStatus.DONE, base.plusHours(4), Duration.ofMinutes(30)));
        tm.createSubTask(new SubTask(epic2, "Подзадача 4", "Описание подзадачи 4", TaskStatus.NEW, base.plusHours(5), Duration.ofMinutes(60)));

        // epic3: (6h, 120m) -> start=base+6h, end=base+8h, duration=120m
        tm.createSubTask(new SubTask(epic3, "Подзадача 5", "Описание подзадачи 5", TaskStatus.IN_PROGRESS, base.plusHours(6), Duration.ofMinutes(120)));

    }
}

