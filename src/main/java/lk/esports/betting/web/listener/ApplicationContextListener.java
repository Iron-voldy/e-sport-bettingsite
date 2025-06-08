package lk.esports.betting.web.listener;

import lk.esports.betting.utils.DatabaseUtil;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(ApplicationContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application context initialized");

        try {
            // Initialize database connection
            boolean dbHealthy = DatabaseUtil.isDatabaseHealthy();
            logger.info("Database health: " + (dbHealthy ? "HEALTHY" : "UNHEALTHY"));

            if (dbHealthy) {
                logger.info("Database initialized successfully");
            } else {
                logger.warning("Database is not healthy. Check MySQL server and connection settings.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during application initialization", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application context being destroyed - cleaning up resources...");

        try {
            // Shutdown MySQL cleanup thread to prevent illegal access errors
            try {
                AbandonedConnectionCleanupThread.checkedShutdown();
                logger.info("MySQL AbandonedConnectionCleanupThread shutdown completed");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error shutting down MySQL cleanup thread", e);
            }

            // Deregister JDBC drivers
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                try {
                    DriverManager.deregisterDriver(driver);
                    logger.info("Deregistered JDBC driver: " + driver.getClass().getName());
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error deregistering JDBC driver: " + driver.getClass().getName(), e);
                }
            }

            // Close EntityManagerFactory
            DatabaseUtil.closeEntityManagerFactory();

            logger.info("Application cleanup completed");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during application cleanup", e);
        }
    }
}