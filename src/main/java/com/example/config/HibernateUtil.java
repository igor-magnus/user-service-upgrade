package com.example.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                sessionFactory = configuration.buildSessionFactory();
                logger.info("SessionFactory created successfully");
            } catch (Exception e) {
                logger.error("Error creating SessionFactory: {}", e.getMessage(), e);
                throw new RuntimeException("Error initializing Hibernate: " + e.getMessage(), e);
            }
        }
        return sessionFactory;
    }

    //Overload method
    public static SessionFactory getSessionFactory(Configuration configuration) {
        if (sessionFactory == null) {
            try {
                sessionFactory = configuration.buildSessionFactory();
                logger.info("SessionFactory created successfully");
            } catch (Exception e) {
                logger.error("Error creating SessionFactory: {}", e.getMessage(), e);
                throw new RuntimeException("Error initializing Hibernate: " + e.getMessage(), e);
            }
        }
        return sessionFactory;
    }


    public static void shutdown() {
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
                logger.info("SessionFactory closed");
            } catch (Exception e) {
                logger.error("Error closing SessionFactory: {}", e.getMessage(), e);
            }
            sessionFactory = null;
        }
    }
}