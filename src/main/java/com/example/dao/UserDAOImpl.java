package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.User;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);
    private final SessionFactory sessionFactory;

    public UserDAOImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public UserDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Optional<User> getById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        } catch (HibernateException e) {
            logger.error("Error getting user by id {}: {}", id, e.getMessage(), e);
            throw new UserDAOException("Error getting user by id " + id, e);
        }
    }

    @Override
    public List<User> getAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaQuery<User> criteriaQuery = session.getCriteriaBuilder().createQuery(User.class);
            criteriaQuery.from(User.class);
            return session.createQuery(criteriaQuery).getResultList(); // Use Criteria API
        } catch (HibernateException e) {
            logger.error("Error getting all users: {}", e.getMessage(), e);
            throw new UserDAOException("Error getting all users", e);
        }
    }


    @Override
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            logger.info("User saved: {}", user);
            return user;
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", e.getMessage(), e);
            throw new UserDAOException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> update(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User existingUser = session.get(User.class, user.getId());
            if (existingUser != null) {
                existingUser.setName(user.getName());
                existingUser.setEmail(user.getEmail());
                existingUser.setAge(user.getAge());
                session.update(existingUser);
                transaction.commit();
                logger.info("User updated: {}", existingUser);
                return Optional.of(existingUser);
            } else {
                transaction.rollback();
                logger.warn("User with id {} not found for update", user.getId());
                return Optional.empty();
            }
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", e.getMessage(), e);
            throw new UserDAOException("Error updating user", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                transaction.commit();
                logger.info("User deleted with id: {}", id);
                return true;
            } else {
                transaction.rollback();
                logger.warn("User with id {} not found for delete", id);
                return false;
            }
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user with id {}: {}", id, e.getMessage(), e);
            throw new UserDAOException("Error deleting user", e);
        }
    }
}