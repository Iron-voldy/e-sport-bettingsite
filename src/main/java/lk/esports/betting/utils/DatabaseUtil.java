package lk.esports.betting.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database utility class for managing database connections and operations
 */
public class DatabaseUtil {

    private static final Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/esports_betting";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "2009928";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // EntityManagerFactory for JPA operations
    private static EntityManagerFactory entityManagerFactory;

    static {
        try {
            // Initialize EntityManagerFactory
            entityManagerFactory = Persistence.createEntityManagerFactory("esportsPU");
            logger.info("EntityManagerFactory initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize EntityManagerFactory", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Get EntityManagerFactory instance
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            synchronized (DatabaseUtil.class) {
                if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
                    entityManagerFactory = Persistence.createEntityManagerFactory("esportsPU");
                }
            }
        }
        return entityManagerFactory;
    }

    /**
     * Create a new EntityManager
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Get a direct JDBC connection to the database
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new SQLException("Database driver not found", e);
        }
    }

    /**
     * Test database connectivity
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
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
            query.getSingleResult();
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Database access verification failed", e);
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Get database connection properties
     */
    public static Properties getDatabaseProperties() {
        Properties props = new Properties();
        props.setProperty("javax.persistence.jdbc.driver", DB_DRIVER);
        props.setProperty("javax.persistence.jdbc.url", DB_URL);
        props.setProperty("javax.persistence.jdbc.user", DB_USERNAME);
        props.setProperty("javax.persistence.jdbc.password", DB_PASSWORD);

        // Connection pool settings
        props.setProperty("hibernate.c3p0.min_size", "5");
        props.setProperty("hibernate.c3p0.max_size", "20");
        props.setProperty("hibernate.c3p0.timeout", "300");
        props.setProperty("hibernate.c3p0.max_statements", "50");
        props.setProperty("hibernate.c3p0.idle_test_period", "3000");

        // Hibernate settings
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");

        return props;
    }

    /**
     * Initialize database schema (if needed)
     */
    public static void initializeSchema() {
        EntityManager em = null;
        try {
            em = createEntityManager();
            em.getTransaction().begin();

            // Check if tables exist by trying to count users
            Query query = em.createNativeQuery("SELECT COUNT(*) FROM users");
            query.getSingleResult();

            em.getTransaction().commit();
            logger.info("Database schema verified successfully");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Database schema verification failed", e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Schema might not exist, but that's okay if using hbm2ddl.auto=update
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
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
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing native query: " + sql, e);
            throw new RuntimeException("Query execution failed", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
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
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing native update: " + sql, e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Update execution failed", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Close EntityManagerFactory
     */
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            logger.info("EntityManagerFactory closed");
        }
    }

    /**
     * Get database statistics
     */
    public static String getDatabaseInfo() {
        EntityManager em = null;
        try {
            em = createEntityManager();

            // Get database version
            Query versionQuery = em.createNativeQuery("SELECT VERSION()");
            String version = (String) versionQuery.getSingleResult();

            // Get current timestamp
            Query timeQuery = em.createNativeQuery("SELECT NOW()");
            String currentTime = timeQuery.getSingleResult().toString();

            return String.format("MySQL Version: %s, Current Time: %s", version, currentTime);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting database info", e);
            return "Database info unavailable";
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Check if database is healthy
     */
    public static boolean isDatabaseHealthy() {
        try {
            return testConnection() && verifyDatabaseAccess();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Database health check failed", e);
            return false;
        }
    }

    /**
     * Get connection pool status (if using connection pooling)
     */
    public static String getConnectionPoolStatus() {
        // This would typically integrate with your connection pool implementation
        // For now, return basic info
        return "Connection pooling managed by Hibernate/C3P0";
    }
}