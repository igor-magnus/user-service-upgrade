package com.example.service;

import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.example.dao.UserDAO;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceUnitTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        User newUser = new User("Test User", "test@example.com", 25);
        when(userDAO.save(any(User.class))).thenReturn(newUser);

        User createdUser = userService.createUser("Test User", "test@example.com", 25);

        assertNotNull(createdUser);
        assertEquals("Test User", createdUser.getName());
        verify(userDAO, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserById() {
        User user = new User("Existing User", "existing@example.com", 30);
        user.setId(1L);
        when(userDAO.getById(1L)).thenReturn(Optional.of(user));

        Optional<User> retrievedUser = userService.getUserById(1L);

        assertTrue(retrievedUser.isPresent());
        assertEquals("Existing User", retrievedUser.get().getName());
        verify(userDAO, times(1)).getById(1L);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User("User 1", "user1@example.com", 20);
        User user2 = new User("User 2", "user2@example.com", 35);
        List<User> userList = Arrays.asList(user1, user2);
        when(userDAO.getAll()).thenReturn(userList);

        List<User> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
        verify(userDAO, times(1)).getAll();
    }

    @Test
    void testUpdateUser() {
        User existingUser = new User("Old Name", "old@example.com", 40);
        existingUser.setId(1L);

        when(userDAO.getById(1L)).thenReturn(Optional.of(existingUser));
        when(userDAO.update(any(User.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0))); // Return updated user

        Optional<User> updatedUser = userService.updateUser(1L, "New Name", "new@example.com", 45);

        assertTrue(updatedUser.isPresent());
        assertEquals("New Name", updatedUser.get().getName());
        verify(userDAO, times(1)).getById(1L);
        verify(userDAO, times(1)).update(any(User.class));
    }

    @Test
    void testDeleteUser() {
        when(userDAO.delete(1L)).thenReturn(true);

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted);
        verify(userDAO, times(1)).delete(1L);
    }

    @Test
    void testCreateUser_ValidationFails() {
        // Simulate a validation failure by providing invalid data
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("", "invalid-email", -5);
        });

        assertTrue(exception.getMessage().contains("Name cannot be blank"));
        assertTrue(exception.getMessage().contains("Invalid email format"));
        assertTrue(exception.getMessage().contains("Age must be at least 0"));

        verify(userDAO, never()).save(any(User.class)); // Ensure DAO is not called
    }
}