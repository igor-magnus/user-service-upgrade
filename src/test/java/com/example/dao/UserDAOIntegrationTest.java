package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.User;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class UserDAOIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private UserDAOImpl userDAO;

    @BeforeAll
    static void setup() {
        // Create a Hibernate configuration based on the Testcontainers settings
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration()
                .setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl())
                .setProperty("hibernate.connection.username", postgreSQLContainer.getUsername())
                .setProperty("hibernate.connection.password", postgreSQLContainer.getPassword())
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop") // Use create-drop for tests
                .setProperty("hibernate.current_session_context_class", "thread")
                .setProperty("hibernate.show_sql", "false") // Disable SQL output during tests
                .addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl(sessionFactory); // Inject SessionFactory
    }


    @AfterEach
    void tearDown() {
        // Clean up the database after each test to ensure isolation
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM User").executeUpdate();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            if (sessionFactory.getCurrentSession().getTransaction() != null) {
                sessionFactory.getCurrentSession().getTransaction().rollback();
            }
            throw e;
        }
    }


    @AfterAll
    static void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        HibernateUtil.shutdown();
    }

    @Test
    void testSaveAndGetById() {
        User user = new User("John Doe", "john.doe@example.com", 30);
        User savedUser = userDAO.save(user);

        assertNotNull(savedUser.getId());
        Optional<User> retrievedUser = userDAO.getById(savedUser.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals(savedUser.getName(), retrievedUser.get().getName());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User("Alice Smith", "alice.smith@example.com", 25);
        User user2 = new User("Bob Johnson", "bob.johnson@example.com", 40);
        userDAO.save(user1);
        userDAO.save(user2);

        List<User> users = userDAO.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() {
        User user = new User("Original Name", "original@example.com", 35);
        User savedUser = userDAO.save(user);
        Long userId = savedUser.getId();

        User updatedUser = new User("Updated Name", "updated@example.com", 45);
        updatedUser.setId(userId); // Set the ID of the user to update

        Optional<User> result = userDAO.update(updatedUser);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());

        Optional<User> retrievedUser = userDAO.getById(userId);
        assertTrue(retrievedUser.isPresent());
        assertEquals("Updated Name", retrievedUser.get().getName());
    }


    @Test
    void testDeleteUser() {
        User user = new User("ToDelete", "delete@example.com", 28);
        User savedUser = userDAO.save(user);
        Long userId = savedUser.getId();

        boolean deleted = userDAO.delete(userId);
        assertTrue(deleted);

        Optional<User> retrievedUser = userDAO.getById(userId);
        assertFalse(retrievedUser.isPresent());
    }

    @Test
    void testGetById_NotFound() {
        Optional<User> user = userDAO.getById(999L);
        assertFalse(user.isPresent());
    }

    @Test
    void testUpdateUser_NotFound() {
        User user = new User();
        user.setId(999L);
        user.setName("Non Existent");
        user.setEmail("email@example.com");
        user.setAge(20);

        Optional<User> updatedUser = userDAO.update(user);
        assertFalse(updatedUser.isPresent());
    }

    @Test
    void testDeleteUser_NotFound() {
        boolean deleted = userDAO.delete(999L);
        assertFalse(deleted);
    }

}