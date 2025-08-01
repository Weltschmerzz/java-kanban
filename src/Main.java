package ru.yandex.practicum.TaskTracker.src;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TaskManager tm = Managers.getDefault();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.println("Создание новой задачи");
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }

                    int epicId = 0;
                    if (taskType == TaskType.SUBTASK) {
                        if (tm.getEpicList().isEmpty()) {
                            System.out.println("Нет созданных эпиков. Сначала создайте эпик!");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        epicId = getEpicId(scanner);
                        if (!tm.getEpicList().contains(tm.getEpicById(epicId))) {
                            System.out.println("Эпик с ID:" + epicId + " не найден.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                    }

                    System.out.print("Введите имя задачи: ");
                    String name = scanner.nextLine();
                    System.out.print("Введите описание задачи: ");
                    String desc = scanner.nextLine();

                    if (taskType == TaskType.TASK) {
                        TaskStatus status = selectTaskStatus(scanner);
                        Task newTask = new Task(name, desc, status);
                        tm.createTask(newTask);
                    } else if (taskType == TaskType.EPIC) {
                        Epic newEpic = new Epic(name, desc);
                        tm.createEpic(newEpic);
                    } else if (taskType == TaskType.SUBTASK) {
                        TaskStatus status = selectTaskStatus(scanner);
                        SubTask newSubTask = new SubTask(tm.getEpicById(epicId), name, desc, status);
                        tm.createSubTask(newSubTask);
                    }
                }
                case 2 -> {
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }
                    if (taskType == TaskType.TASK) {

                        for (Task task : tm.getTaskList()) {
                            System.out.println(task);
                        }
                    } else if (taskType == TaskType.EPIC) {
                        for (Epic epic : tm.getEpicList()) {
                            System.out.println(epic);
                        }
                    } else if (taskType == TaskType.SUBTASK) {
                        int select = selectOptionsShowSubTaskList(scanner);
                        while (select == 0) {
                            System.out.println("Повторите попытку: ");
                            select = selectOptionsShowSubTaskList(scanner);
                        }
                        if (select == 1) {
                            if (tm.getEpicList().isEmpty()) {
                                System.out.println("Нет созданных эпиков. Сначала создайте эпик!");
                                System.out.println("*".repeat(25));
                                break;
                            }
                            int epicId = getEpicId(scanner);
                            if (!tm.getEpicList().contains(tm.getEpicById(epicId))) {
                                System.out.println("Эпик с ID:" + epicId + " не найден.");
                                System.out.println("*".repeat(25));
                                break;
                            }
                            for (SubTask subTask : tm.getSubTaskListByEpic(epicId)) {
                                System.out.println(subTask);
                            }
                        } else if (select == 2) {

                            for (SubTask subTask : tm.getSubTaskList()) {
                                System.out.println(subTask);
                            }
                        }
                    }
                }
                case 3 -> {
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }
                    if (taskType == TaskType.TASK) {
                        tm.clearTaskList();
                    } else if (taskType == TaskType.EPIC) {
                        System.out.println("Если удалить все Эпики, все подзадачи также будут удалены!");
                        System.out.println("Вы хотите продолжить?");
                        System.out.println("1 - да");
                        System.out.println("2 - нет");
                        int select = scanner.nextInt();
                        if (select == 1) {
                            tm.clearEpicList();
                        }
                    } else if (taskType == TaskType.SUBTASK) {
                        if (tm.getSubTaskList().isEmpty()) {
                            System.out.println("Нет созданных подзадачи. Сначала создайте подзадачу!");
                            System.out.println("*".repeat(25));
                            break;
                        }

                        System.out.println("Вы хотите удалить все подзадачи или подзадачи определенного эпика?");
                        System.out.println("1 - Удалить все подзадачи");
                        System.out.println("2 - Удалить подзадачи определенного эпика");
                        int select = scanner.nextInt();
                        scanner.nextLine();
                        if (select == 1) {
                            tm.clearSubTaskList();
                        } else if (select == 2) {
                            int epicId = getEpicId(scanner);
                            if (!tm.getEpicList().contains(tm.getEpicById(epicId))) {
                                System.out.println("Эпик с ID:" + epicId + " не найден.");
                                System.out.println("*".repeat(25));
                                break;
                            }
                            tm.clearSubTaskListForEpic(epicId);
                        }
                    }
                }
                case 4 -> {
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }

                    if (taskType == TaskType.TASK) {
                        int taskId = getTaskId(scanner);
                        if (!tm.getTaskList().contains(tm.getTaskById(taskId))) {
                            System.out.println("Задача с ID:" + taskId + " не найден.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        System.out.println(tm.getTaskById(taskId));
                    } else if (taskType == TaskType.EPIC) {
                        int epicId = getEpicId(scanner);
                        if (!tm.getEpicList().contains(tm.getEpicById(epicId))) {
                            System.out.println("Эпик с ID:" + epicId + " не найден.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        System.out.println(tm.getEpicById(epicId));
                    } else if (taskType == TaskType.SUBTASK) {
                        int subTaskId = getSubTaskId(scanner);
                        if (!tm.getSubTaskList().contains(tm.getSubTaskById(subTaskId))) {
                            System.out.println("Подзадача с ID:" + subTaskId + " не найдена.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        System.out.println(tm.getSubTaskById(subTaskId));
                    }
                }
                case 5 -> {
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }

                    if (taskType == TaskType.TASK) {
                        int taskId = getTaskId(scanner);
                        if (!tm.getTaskList().contains(tm.getTaskById(taskId))) {
                            System.out.println("Задача с ID:" + taskId + " не найден.");
                            System.out.println("*".repeat(25));
                            break;
                        }

                        System.out.print("Введите новое имя задачи: ");
                        String taskName = scanner.nextLine();
                        System.out.print("Введите новое описание задачи: ");
                        String taskDesc = scanner.nextLine();
                        TaskStatus taskStatus = selectTaskStatus(scanner);
                        Task updatedTask = new Task(taskName, taskDesc, taskStatus);
                        updatedTask.setId(taskId);
                        tm.updateTask(updatedTask);

                    } else if (taskType == TaskType.EPIC) {
                        int epicId = getEpicId(scanner);
                        if (!tm.getEpicList().contains(tm.getEpicById(epicId))) {
                            System.out.println("Эпик с ID:" + epicId + " не найден.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        System.out.print("Введите новое имя эпика: ");
                        String epicName = scanner.nextLine();
                        System.out.print("Введите новое описание эпика: ");
                        String epicDesc = scanner.nextLine();
                        Epic updatedEpic = new Epic(epicName, epicDesc);
                        updatedEpic.setId(epicId);
                        tm.updateEpic(updatedEpic);

                    } else if (taskType == TaskType.SUBTASK) {
                        int subTaskId = getSubTaskId(scanner);
                        if (!tm.getSubTaskList().contains(tm.getSubTaskById(subTaskId))) {
                            System.out.println("Подзадача с ID:" + subTaskId + " не найдена.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        System.out.print("Введите новое имя подзадачи: ");
                        String subTaskName = scanner.nextLine();
                        System.out.print("Введите новое описание подзадачи: ");
                        String subTaskDesc = scanner.nextLine();
                        TaskStatus subTaskStatus = selectTaskStatus(scanner);
                        SubTask updatedSubTask = new SubTask(tm.getEpicById(tm.getSubTaskById(subTaskId).getEpicId()),
                                subTaskName, subTaskDesc, subTaskStatus);
                        updatedSubTask.setId(subTaskId);
                        tm.updateSubTask(updatedSubTask);
                    }
                }
                case 6 -> {
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }
                    if (taskType == TaskType.TASK) {
                        int taskId = getTaskId(scanner);
                        if (!tm.getTaskList().contains(tm.getTaskById(taskId))) {
                            System.out.println("Задача с ID:" + taskId + " не найден.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        tm.deleteTask(taskId);
                    } else if (taskType == TaskType.EPIC) {
                        System.out.println("Если удалить Эпики, все его подзадачи также будут удалены.");
                        System.out.println("Вы хотите продолжить?");
                        System.out.println("1 - да");
                        System.out.println("2 - нет");
                        int option = scanner.nextInt();
                        scanner.nextLine();
                        if (option == 1) {
                            int epicId = getEpicId(scanner);
                            if (!tm.getEpicList().contains(tm.getEpicById(epicId))) {
                                System.out.println("Эпик с ID:" + epicId + " не найден.");
                                System.out.println("*".repeat(25));
                                break;
                            }
                            tm.deleteEpic(epicId);
                        }
                    } else if (taskType == TaskType.SUBTASK) {
                        int subTaskId = getSubTaskId(scanner);
                        if (!tm.getSubTaskList().contains(tm.getSubTaskById(subTaskId))) {
                            System.out.println("Подзадача с ID:" + subTaskId + " не найдена.");
                            System.out.println("*".repeat(25));
                            break;
                        }
                        tm.deleteSubTask(subTaskId);
                    }
                }
                case 7 -> {
                    if (tm.getHistory().isEmpty()) {
                        System.out.println("История просмотров пуста!");
                    } else {
                        for (Task task : tm.getHistory()) {
                            System.out.println(task);
                        }
                    }
                }
                case 8 -> {
                    return;
                }
                default -> System.out.println("Такого пункта нет в меню.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("Выберете команду:");
        System.out.println("1. Создать задачу.");
        System.out.println("2. Посмотреть список задач по типу.");
        System.out.println("3. Очистить список задач по типу.");
        System.out.println("4. Посмотреть задачу по ID.");
        System.out.println("5. Обновить задачу по ID.");
        System.out.println("6. Удалить задачу по ID.");
        System.out.println("7. История просмотров.");
        System.out.println("8. Выход.");
    }

    private static TaskType selectTaskType(Scanner sc) {
        System.out.println("Выберете тип задачи:");
        System.out.println("1 - Задача");
        System.out.println("2 - Эпик");
        System.out.println("3 - Подзадача (для Эпика)");
        int typeCode = sc.nextInt();
        sc.nextLine();
        switch (typeCode) {
            case 1 -> {
                return TaskType.TASK;
            }
            case 2 -> {
                return TaskType.EPIC;
            }
            case 3 -> {
                return TaskType.SUBTASK;
            }
            default -> {
                System.out.println("Неверный тип, укажите верный тип задачи.");
                return null;
            }
        }
    }

    private static TaskStatus selectTaskStatus(Scanner sc) {
        while (true) {
            System.out.println("Выберете статус:");
            System.out.println("1 - NEW");
            System.out.println("2 - IN_PROGRESS");
            System.out.println("3 - DONE");
            int statusCode = sc.nextInt();
            sc.nextLine();
            switch (statusCode) {
                case 1 -> {
                    return TaskStatus.NEW;
                }
                case 2 -> {
                    return TaskStatus.IN_PROGRESS;
                }
                case 3 -> {
                    return TaskStatus.DONE;
                }
                default -> System.out.println("Неверное значение статуса. Повторите попытку.");
            }
        }
    }

    private static int getEpicId(Scanner sc) {
        System.out.print("Введите ID Эпика: ");
        int epicId = sc.nextInt();
        sc.nextLine();
        return epicId;
    }

    private static int getTaskId(Scanner sc) {
        System.out.print("Введите ID Задачи: ");
        int taskId = sc.nextInt();
        sc.nextLine();
        return taskId;
    }

    private static int getSubTaskId(Scanner sc) {
        System.out.print("Введите ID Подзадачи: ");
        int subTaskId = sc.nextInt();
        sc.nextLine();
        return subTaskId;
    }

    private static int selectOptionsShowSubTaskList(Scanner sc) {
        System.out.println("Выберете вариант отображения списка подзадач:");
        System.out.println("1 - Показать подзадачи одного Эпика");
        System.out.println("2 - Показать подзадачи всех Эпиков");
        int choice = sc.nextInt();
        sc.nextLine();
        switch (choice) {
            case 1 -> {
                return 1;
            }
            case 2 -> {
                return 2;
            }
            default -> {
                System.out.println("Неверный пункт меню, выберете корректные пункт.");
                return 0;
            }
        }
    }
}
