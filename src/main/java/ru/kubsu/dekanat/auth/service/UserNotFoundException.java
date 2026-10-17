package ru.kubsu.dekanat.auth.service;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Пользователь с id " + id + " не найден");
    }

}
