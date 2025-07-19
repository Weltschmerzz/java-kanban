package ru.yandex.practicum.TaskTracker.test;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.TaskTracker.src.*;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private static TaskManager tm;


    @BeforeEach
    public void beforeEach() {
        addTestTasks();
    }

    //Убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных
    @Test
    public void shouldStoreOriginalTaskDataInHistory() {
        String originalName = tm.getTaskById(4).getName(); //добавляем в историю
        assertEquals(1, tm.getHistory().size()); //проверяем обновление
        Task newTask = new Task("NewTask", "NewDesc", TaskStatus.DONE);
        newTask.setId(4);
        tm.updateTask(newTask); //добавляем новую задачу c id 4
        assertEquals(originalName, tm.getHistory().getFirst().getName()); //проверяем что в истории сохранилась старая задача
    }

    //Проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    public void shouldNotConflictManualIdAndAutomaticId() {
        Task newTask = new Task("N", "D", TaskStatus.NEW);
        newTask.setId(16);
        tm.createTask(newTask);
        assertEquals(newTask, tm.getTaskById(16));

        Task newTask1 = new Task("Name", "Desc", TaskStatus.IN_PROGRESS);
        tm.createTask(newTask1);
        assertEquals(newTask1, tm.getTaskById(17));
    }


    //Проверьте, что экземпляры класса Task равны друг другу, если равен их id
    //Проверьте, что наследники класса Task равны друг другу, если равен их id
    @Test
    public void shouldTasksEqualsIfEqualItsIDs() {
        //Согласно условия ФЗ-4 переопределенный equals в супер-классе сравнивает только ID!
        Task task1 = new Task("Задача 2", "Описание задачи 2", TaskStatus.IN_PROGRESS);
        task1.setId(3);
        Task task2 = new Task("Другая задача", "Другое описание", TaskStatus.DONE);
        task2.setId(3);
        assertEquals(task1, task2);

        Epic epic1 = new Epic("Эпик 2", "Описание эпика 2");
        epic1.setId(7);
        Epic epic2 = new Epic("Другой эпик", "Другое описание");
        epic2.setId(7);
        assertEquals(epic1, epic2);

        Epic epicForSubTask = tm.getEpicById(6); // Любой существующий эпик
        SubTask subTask1 = new SubTask(epicForSubTask, "Подзадача 4", "Описание подзадачи 4", TaskStatus.NEW);
        subTask1.setId(14);
        SubTask subTask2 = new SubTask(epicForSubTask, "Другая подзадача", "Другое описание", TaskStatus.DONE);
        subTask2.setId(14);
        assertEquals(subTask1, subTask2);
    }

    //Проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи
    //Проверьте, что объект Subtask нельзя сделать своим же эпиком
    @Test
    public void shouldRejectAddEpicInItsSubTaskList() {
        Epic epic = tm.getEpicById(7);
        SubTask invalid = new SubTask(epic, "NewTestSubTask", "NewTestSubTaskDescription", TaskStatus.NEW);
        invalid.setId(7);
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

    //Убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    public void shouldManagerGetCorrectObjectInMemoryTaskManager() {
        assertNotNull(tm);
        assertInstanceOf(InMemoryTaskManager.class, tm);
    }

    @Test
    public void shouldManagerGetCorrectObjectInMemoryHistoryManager() {
        HistoryManager hm = Managers.getDefaultHistory();
        assertNotNull(hm);
        assertInstanceOf(InMemoryHistoryManager.class, hm);
        Task task = new Task("Task", "Desc", TaskStatus.IN_PROGRESS);
        hm.add(task);
        assertEquals(1, hm.getHistory().size());
        assertEquals(task, hm.getHistory().getFirst());
    }

    //Проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
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
        assertEquals(5, tm.getHistory().getLast().getId()); //проверить добавления повторяющегося значения в конец
        tm.getEpicById(10);
        assertEquals(10, tm.getHistory().getLast().getId()); //проверить добавления повторяющегося значения в конец
        tm.getSubTaskById(15);
        assertEquals(15, tm.getHistory().getLast().getId()); //проверить добавления повторяющегося значения в конец
        assertEquals(6, tm.getHistory().size()); //проверить удаление дублей

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

    //Создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджере.
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
    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    public void shouldReturnNewTaskById() {
        Task updatedTask = new Task("NewTask", "NewTaskDesc", TaskStatus.DONE);
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
        SubTask updatedSubTask = new SubTask(tm.getEpicById(8), "NewSubTask", "NewSubTaskDesc", TaskStatus.DONE);
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

    //Очистить список задач по типу
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

    //Посмотреть задачу по ID
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

    private static void addTestTasks() {
        tm = Managers.getDefault();

        tm.createTask(new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW));
        tm.createTask(new Task("Задача 2", "Описание задачи 2", TaskStatus.IN_PROGRESS));
        tm.createTask(new Task("Задача 3", "Описание задачи 3", TaskStatus.DONE));
        tm.createTask(new Task("Задача 4", "Описание задачи 4", TaskStatus.NEW));
        tm.createTask(new Task("Задача 5", "Описание задачи 5", TaskStatus.IN_PROGRESS));

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        Epic epic3 = new Epic("Эпик 3", "Описание эпика 3");
        Epic epic4 = new Epic("Эпик 4", "Описание эпика 4");
        Epic epic5 = new Epic("Эпик 5", "Описание эпика 5");
        tm.createEpic(epic1);
        tm.createEpic(epic2);
        tm.createEpic(epic3);
        tm.createEpic(epic4);
        tm.createEpic(epic5);

        tm.createSubTask(new SubTask(epic1, "Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW));
        tm.createSubTask(new SubTask(epic1, "Подзадача 2", "Описание подзадачи 2", TaskStatus.IN_PROGRESS));
        tm.createSubTask(new SubTask(epic2, "Подзадача 3", "Описание подзадачи 3", TaskStatus.DONE));
        tm.createSubTask(new SubTask(epic2, "Подзадача 4", "Описание подзадачи 4", TaskStatus.NEW));
        tm.createSubTask(new SubTask(epic3, "Подзадача 5", "Описание подзадачи 5", TaskStatus.IN_PROGRESS));
    }
}

