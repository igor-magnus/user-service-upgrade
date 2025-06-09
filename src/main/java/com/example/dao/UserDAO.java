package com.example.dao;

import com.example.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> getById(Long id);
    List<User> getAll();
    User save(User user);
    Optional<User> update(User user);
    boolean delete(Long id);
}