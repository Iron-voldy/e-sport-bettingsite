package lk.esports.betting.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database utility class for managing database connections and operations
 */
public class DatabaseUtil {

    private static final Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/esports_betting?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "2009928";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // EntityManagerFactory for JPA operations
    private static volatile EntityManagerFactory entityManagerFactory;
    private static boolean initializationFailed = false;
    private static final Object lock = new Object();

    // Initialize EntityManagerFactory with proper error handling
    static {
        try {
            initializeEntityManagerFactory();
            logger.info("EntityManagerFactory initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize EntityManagerFactory", e);
            initializationFailed = true;
            // Don't throw exception here to allow application to start
        }
    }

    private static void initializeEntityManagerFactory() {
        synchronized (lock) {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                return;
            }

            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.driver", DB_DRIVER);
            properties.put("jakarta.persistence.jdbc.url", DB_URL);
            properties.put("jakarta.persistence.jdbc.user", DB_USERNAME);
            properties.put("jakarta.persistence.jdbc.password", DB_PASSWORD);

            // Hibernate specific properties
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.show_sql", "false"); // Changed to false for production
            properties.put("hibernate.format_sql", "false");
            properties.put("hibernate.cache.use_second_level_cache", "false");
            properties.put("hibernate.cache.use_query_cache", "false");

            // Connection pool settings
            properties.put("hibernate.c3p0.min_size", "5");
            properties.put("hibernate.c3p0.max_size", "20");
            properties.put("hibernate.c3p0.timeout", "300");
            properties.put("hibernate.c3p0.max_statements", "50");
            properties.put("hibernate.c3p0.idle_test_period", "3000");
            properties.put("hibernate.c3p0.acquire_increment", "1");
            properties.put("hibernate.c3p0.max_idle_time", "300");

            // Transaction settings
            properties.put("hibernate.connection.autocommit", "false");
            properties.put("hibernate.connection.isolation", "2"); // READ_COMMITTED

            try {
                entityManagerFactory = Persistence.createEntityManagerFactory("esportsPU", properties);
                initializationFailed = false;
                logger.info("EntityManagerFactory created successfully");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to create EntityManagerFactory", e);
                initializationFailed = true;
                throw e;
            }
        }
    }

    /**
     * Get EntityManagerFactory instance
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (initializationFailed || entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            synchronized (lock) {
                if (initializationFailed || entityManagerFactory == null || !entityManagerFactory.isOpen()) {
                    try {
                        initializeEntityManagerFactory();
                        logger.info("EntityManagerFactory re-initialized successfully");
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to re-initialize EntityManagerFactory", e);
                        throw new RuntimeException("Database not available", e);
                    }
                }
            }
        }
        return entityManagerFactory;
    }

    /**
     * Create a new EntityManager
     */
    public static EntityManager createEntityManager() {
        try {
            EntityManagerFactory emf = getEntityManagerFactory();
            EntityManager em = emf.createEntityManager();
            logger.fine("EntityManager created successfully");
            return em;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create EntityManager", e);
            throw new RuntimeException("Cannot create EntityManager", e);
        }
    }

    /**
     * Get a direct JDBC connection to the database
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            logger.fine("JDBC Connection created successfully");
            return conn;
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create JDBC connection", e);
            throw e;
        }
    }

    /**
     * Test database connectivity
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn != null && !conn.isClosed() && conn.isValid(5);
            logger.fine("Database connection test: " + (isValid ? "SUCCESS" : "FAILED"));
            return isValid;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }

    /**
     * Execute a simple query to verify database is accessible
     */
    public static boolean verifyDatabaseAccess() {
        EntityManager em = null;
        try {
            em = createEntityManager();
            Query query = em.createNativeQuery("SELECT 1");
            Object result = query.getSingleResult();
            logger.fine("Database access verification: SUCCESS");
            return result != null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Database access verification failed", e);
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error closing EntityManager", e);
                }
            }
        }
    }

    /**
     * Initialize database schema (if needed)
     */
    public static void initializeSchema() {
        EntityManager em = null;
        try {
            em = createEntityManager();

            // Check if users table exists by trying to count users
            Query query = em.createNativeQuery("SELECT COUNT(*) FROM users");
            Object result = query.getSingleResult();

            logger.info("Database schema verified successfully. Users table has " + result + " records.");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Database schema verification failed: " + e.getMessage());
            // Schema might not exist, but that's okay if using hbm2ddl.auto=update
            logger.info("Database schema will be created/updated automatically by Hibernate");
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error closing EntityManager", e);
                }
            }
        }
    }

    /**
     * Execute a native SQL query
     */
    public static Object executeNativeQuery(String sql) {
        EntityManager em = null;
        try {
            em = createEntityManager();
            Query query = em.createNativeQuery(sql);
            Object result = query.getSingleResult();
            logger.fine("Native query executed successfully: " + sql);
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing native query: " + sql, e);
            throw new RuntimeException("Query execution failed", e);
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error closing EntityManager", e);
                }
            }
        }
    }

    /**
     * Execute a native SQL update/insert/delete
     */
    public static int executeNativeUpdate(String sql) {
        EntityManager em = null;
        try {
            em = createEntityManager();
            em.getTransaction().begin();

            Query query = em.createNativeQuery(sql);
            int result = query.executeUpdate();

            em.getTransaction().commit();
            logger.fine("Native update executed successfully: " + sql);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing native update: " + sql, e);
            if (em != null && em.getTransaction().isActive()) {
                try {
                    em.getTransaction().rollback();
                } catch (Exception rollbackEx) {
                    logger.log(Level.WARNING, "Error rolling back transaction", rollbackEx);
                }
            }
            throw new RuntimeException("Update execution failed", e);
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error closing EntityManager", e);
                }
            }
        }
    }

    /**
     * Close EntityManagerFactory
     */
    public static void closeEntityManagerFactory() {
        synchronized (lock) {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                try {
                    entityManagerFactory.close();
                    logger.info("EntityManagerFactory closed successfully");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error closing EntityManagerFactory", e);
                } finally {
                    entityManagerFactory = null;
                }
            }
        }
    }

    /**
     * Get database statistics
     */
    public static String getDatabaseInfo() {
        try (Connection conn = getConnection()) {
            // Get database version
            String version = conn.getMetaData().getDatabaseProductVersion();
            String productName = conn.getMetaData().getDatabaseProductName();

            return String.format("%s Version: %s", productName, version);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting database info", e);
            return "Database info unavailable: " + e.getMessage();
        }
    }

    /**
     * Check if database is healthy
     */
    public static boolean isDatabaseHealthy() {
        try {
            boolean connTest = testConnection();
            boolean accessTest = verifyDatabaseAccess();
            boolean result = connTest && accessTest;

            logger.fine("Database health check - Connection: " + connTest +
                    ", Access: " + accessTest + ", Overall: " + result);
            return result;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Database health check failed", e);
            return false;
        }
    }

    /**
     * Get connection pool status
     */
    public static String getConnectionPoolStatus() {
        try {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                return "EntityManagerFactory: OPEN, Connection pooling: C3P0 managed by Hibernate";
            } else {
                return "EntityManagerFactory: CLOSED";
            }
        } catch (Exception e) {
            return "Connection pool status: ERROR - " + e.getMessage();
        }
    }

    /**
     * Perform cleanup operations
     */
    public static void cleanup() {
        logger.info("Performing database cleanup...");
        closeEntityManagerFactory();
        logger.info("Database cleanup completed");
    }
}