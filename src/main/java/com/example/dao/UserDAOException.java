package com.example.dao;

public class UserDAOException extends RuntimeException {
    public UserDAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDAOException(String message) {
        super(message);
    }
}