package ru.yandex.practicum.TaskTracker.src;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();
        Scanner scanner = new Scanner(System.in);

        //TestDataInitializer.initializeTestData(tm);  // заполнение тестовыми данными

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
                        if (tm.isEpicListEmpty()) {
                            break;
                        }
                        epicId = getEpicId(scanner);
                        if (tm.checkEpicId(epicId)) {
                            break;
                        }
                    }

                    System.out.print("Введите имя задачи: ");
                    String name = scanner.nextLine();
                    System.out.print("Введите описание задачи: ");
                    String desc = scanner.nextLine();
                    if (taskType == TaskType.TASK) {
                        TaskStatus status = selectTaskStatus(scanner);
                        tm.createTask(name, desc, status);
                    } else if (taskType == TaskType.EPIC) {
                        tm.createEpic(name, desc);
                    } else if (taskType == TaskType.SUBTASK) {
                        TaskStatus status = selectTaskStatus(scanner);
                        tm.createSubTask(epicId, name, desc, status);
                    }
                }
                case 2 -> {
                    TaskType taskType = selectTaskType(scanner);
                    while (taskType == null) {
                        System.out.println("Повторите попытку: ");
                        taskType = selectTaskType(scanner);
                    }
                    if (taskType == TaskType.TASK) {
                        tm.getTaskList();
                    } else if (taskType == TaskType.EPIC) {
                        tm.getEpicList();
                    } else if (taskType == TaskType.SUBTASK) {

                        int select = selectOptionsShowSubTaskList(scanner);
                        while (select == 0) {
                            System.out.println("Повторите попытку: ");
                            select = selectOptionsShowSubTaskList(scanner);
                        }
                        if (select == 1) {
                            if (tm.isEpicListEmpty()) {
                                break;
                            }
                            int epicId = getEpicId(scanner);
                            if (!tm.checkEpicId(epicId)) {
                                tm.getSubTaskListByEpic(epicId);
                            }
                        } else if (select == 2) {
                            tm.getSubTaskList();
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
                        System.out.println("Если удалить все Эпики, все подзадачи также будут удалены.");
                        System.out.println("Вы хотите продолжить?");
                        System.out.println("1 - да");
                        System.out.println("2 - нет");
                        int select = scanner.nextInt();
                        if (select == 1) {
                            tm.clearEpicList();
                        }
                    } else if (taskType == TaskType.SUBTASK) {
                        if (tm.isSubTaskListEmpty()) {
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
                            if (!tm.checkEpicId(epicId)) {
                                tm.clearSubTaskListForEpic(epicId);
                            }
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
                        tm.getTaskById(scanner);
                    } else if (taskType == TaskType.EPIC) {
                        tm.getEpicById(scanner);
                    } else if (taskType == TaskType.SUBTASK) {
                        tm.getSubTaskById(scanner);
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
                        if (tm.checkTaskId(taskId)) {
                            System.out.print("Введите новое имя задачи: ");
                            String taskName = scanner.nextLine();
                            System.out.print("Введите новое описание задачи: ");
                            String taskDesc = scanner.nextLine();
                            TaskStatus status = selectTaskStatus(scanner);
                            tm.updateTask(taskId, taskName, taskDesc, status);
                        }
                    } else if (taskType == TaskType.EPIC) {
                        if (tm.isEpicListEmpty()) {
                            break;
                        }
                        int epicId = getEpicId(scanner);
                        if (!tm.checkEpicId(epicId)) {
                            System.out.print("Введите новое имя эпика: ");
                            String epicName = scanner.nextLine();
                            System.out.print("Введите новое описание эпика: ");
                            String epicDesc = scanner.nextLine();
                            tm.updateEpic(epicId, epicName, epicDesc);
                        }
                    } else if (taskType == TaskType.SUBTASK) {
                        int subTaskId = getSubTaskId(scanner);
                        if (tm.checkSubTaskId(subTaskId)) {
                            System.out.print("Введите новое имя подзадачи: ");
                            String subTaskName = scanner.nextLine();
                            System.out.print("Введите новое описание подзадачи: ");
                            String subTaskDesc = scanner.nextLine();
                            TaskStatus status = selectTaskStatus(scanner);
                            tm.updateSubTask(subTaskId, subTaskName, subTaskDesc, status);
                        }
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
                        if (tm.checkTaskId(taskId)) {
                            tm.deleteTask(taskId);
                        }
                    } else if (taskType == TaskType.EPIC) {
                        System.out.println("Если удалить Эпики, все его подзадачи также будут удалены.");
                        System.out.println("Вы хотите продолжить?");
                        System.out.println("1 - да");
                        System.out.println("2 - нет");
                        int option = scanner.nextInt();
                        scanner.nextLine();
                        if (option == 1) {
                            int epicId = getEpicId(scanner);
                            if (!tm.checkEpicId(epicId)) {
                                tm.deleteEpic(epicId);
                            }
                        }
                    } else if (taskType == TaskType.SUBTASK) {
                        int subTaskId = getSubTaskId(scanner);
                        if (tm.checkSubTaskId(subTaskId)) {
                            tm.deleteSubTask(subTaskId);
                        }
                    }
                }
                //отладочный метод для проверки пересчета статусов
                case 13 -> {
                    int id = getSubTaskId(scanner);
                    System.out.println("Выбери статус подзадачи:");
                    System.out.println("NEW");
                    System.out.println("IN_PROGRESS");
                    System.out.println("DONE");
                    String status = scanner.nextLine();
                    TaskStatus state = TaskStatus.valueOf(status);
                    tm.changeStatus(id, state);

                }
                case 7 -> {
                    return;
                }
                default -> System.out.println("Такого пункта нет в меню.");
            }
        }
    }

    public static void printMenu() {
        System.out.println("Выберете команду:");
        System.out.println("1. Создать задачу.");
        System.out.println("2. Посмотреть список задач по типу.");
        System.out.println("3. Очистить список задач по типу.");
        System.out.println("4. Посмотреть задачу по ID.");
        System.out.println("5. Обновить задачу по ID.");
        System.out.println("6. Удалить задачу по ID.");
        System.out.println("7. Выход.");
    }

    public static TaskType selectTaskType(Scanner sc) {
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

    public static TaskStatus selectTaskStatus(Scanner sc) {
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
                default -> {
                    System.out.println("Неверное значение статуса. Повторите попытку.");
                }
            }
        }
    }

    public static int getEpicId(Scanner sc) {
        System.out.print("Введите ID Эпика: ");
        int epicId = sc.nextInt();
        sc.nextLine();
        return epicId;
    }

    public static int getTaskId(Scanner sc) {
        System.out.print("Введите ID Задачи: ");
        int taskId = sc.nextInt();
        sc.nextLine();
        return taskId;
    }

    public static int getSubTaskId(Scanner sc) {
        System.out.print("Введите ID Подзадачи: ");
        int subTaskId = sc.nextInt();
        sc.nextLine();
        return subTaskId;
    }

    public static int selectOptionsShowSubTaskList(Scanner sc) {
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
