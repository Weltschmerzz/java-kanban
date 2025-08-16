package ru.yandex.practicum.TaskTracker.src;

public class FileManagerException extends RuntimeException {

    public FileManagerException(String message) {
        super(message);
    }

    public FileManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
