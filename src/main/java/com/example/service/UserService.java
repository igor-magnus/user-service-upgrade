package com.example.service;

import com.example.dao.UserDAOException;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.dao.UserDAO;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private void validateUser(User user) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<User> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            throw new IllegalArgumentException("Invalid user data:\n" + sb.toString());
        }
    }

    public User createUser(String name, String email, Integer age) {
        User newUser = new User(name, email, age);
        validateUser(newUser); // Валидация перед сохранением

        try {
            return userDAO.save(newUser);
        } catch (UserDAOException e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to create user: " + e.getMessage(), e); //Use custom service exception
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating user: {}", e.getMessage(), e);
            throw e; // Re-throw the IllegalArgumentException
        }
    }

    public Optional<User> getUserById(Long id) {
        try {
            return userDAO.getById(id);
        }  catch (UserDAOException e) {
            logger.error("Error getting user by id {}: {}", id, e.getMessage(), e);
            throw new UserServiceException("Failed to get user by id", e);
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDAO.getAll();
        } catch (UserDAOException e) {
            logger.error("Error getting all users: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to get all users", e);
        }
    }

    public Optional<User> updateUser(Long id, String name, String email, Integer age) {
        try {
            Optional<User> existingUser = userDAO.getById(id);
            if (existingUser.isPresent()) {
                User userToUpdate = existingUser.get();
                userToUpdate.setName(name);
                userToUpdate.setEmail(email);
                userToUpdate.setAge(age);
                validateUser(userToUpdate);  // Validate before updating
                return userDAO.update(userToUpdate);
            } else {
                return Optional.empty();
            }
        }  catch (UserDAOException e) {
            logger.error("Error updating user with id {}: {}", id, e.getMessage(), e);
            throw new UserServiceException("Failed to update user", e);
        }catch (IllegalArgumentException e) {
            logger.error("Validation error updating user: {}", e.getMessage(), e);
            throw e; // Re-throw the IllegalArgumentException
        }
    }

    public boolean deleteUser(Long id) {
        try {
            return userDAO.delete(id);
        }  catch (UserDAOException e) {
            logger.error("Error deleting user with id {}: {}", id, e.getMessage(), e);
            throw new UserServiceException("Failed to delete user", e);
        }
    }
}