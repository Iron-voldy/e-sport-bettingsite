package lk.esports.betting.web.listener;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.utils.DatabaseUtil;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebListener
public class EJBContainerListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(EJBContainerListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("EJB Container Listener - Initializing application...");

        try {
            // Initialize JNDI context
            initializeJNDIContext();

            // Test database connectivity
            testDatabaseConnection();

            // Initialize and register EJBs
            initializeEJBServices(sce);

            logger.info("Application initialization completed successfully");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize application", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("EJB Container Listener - Application shutting down...");

        try {
            // Cleanup resources
            DatabaseUtil.closeEntityManagerFactory();
            logger.info("Application shutdown completed");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during application shutdown", e);
        }
    }

    private void initializeJNDIContext() {
        try {
            Context ctx = new InitialContext();
            logger.info("JNDI Context initialized successfully");

            // List available JNDI names for debugging
            try {
                javax.naming.NamingEnumeration<javax.naming.NameClassPair> list = ctx.list("java:comp/env");
                logger.info("Available JNDI resources:");
                while (list.hasMore()) {
                    javax.naming.NameClassPair pair = list.next();
                    logger.info("  - " + pair.getName() + " : " + pair.getClassName());
                }
            } catch (Exception e) {
                logger.info("Could not list JNDI resources: " + e.getMessage());
            }

        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Failed to initialize JNDI context", e);
        }
    }

    private void testDatabaseConnection() {
        try {
            boolean dbHealthy = DatabaseUtil.isDatabaseHealthy();
            logger.info("Database connectivity test: " + (dbHealthy ? "SUCCESS" : "FAILED"));

            if (!dbHealthy) {
                logger.warning("Database is not accessible. Check MySQL server and connection settings.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database connectivity test failed", e);
        }
    }

    private void initializeEJBServices(ServletContextEvent sce) {
        try {
            Context ctx = new InitialContext();

            // Try to lookup and initialize EJB services
            UserService userService = lookupUserService(ctx);
            MatchService matchService = lookupMatchService(ctx);
            BettingService bettingService = lookupBettingService(ctx);

            // Store services in servlet context for global access
            sce.getServletContext().setAttribute("userService", userService);
            sce.getServletContext().setAttribute("matchService", matchService);
            sce.getServletContext().setAttribute("bettingService", bettingService);

            logger.info("EJB services initialized and stored in servlet context");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize EJB services", e);
        }
    }

    private UserService lookupUserService(Context ctx) {
        String[] lookupPaths = {
                "java:comp/env/ejb/UserService",
                "java:global/ESportsBetting/UserServiceBean!lk.esports.betting.ejb.local.UserService",
                "java:app/ESportsBetting/UserServiceBean!lk.esports.betting.ejb.local.UserService",
                "java:module/UserServiceBean!lk.esports.betting.ejb.local.UserService",
                "ejb/UserService"
        };

        for (String path : lookupPaths) {
            try {
                UserService service = (UserService) ctx.lookup(path);
                if (service != null) {
                    logger.info("UserService found at: " + path);
                    return service;
                }
            } catch (NamingException e) {
                logger.fine("UserService not found at: " + path + " - " + e.getMessage());
            }
        }

        logger.severe("UserService not found at any lookup path!");
        return null;
    }

    private MatchService lookupMatchService(Context ctx) {
        String[] lookupPaths = {
                "java:comp/env/ejb/MatchService",
                "java:global/ESportsBetting/MatchServiceBean!lk.esports.betting.ejb.local.MatchService",
                "java:app/ESportsBetting/MatchServiceBean!lk.esports.betting.ejb.local.MatchService",
                "java:module/MatchServiceBean!lk.esports.betting.ejb.local.MatchService",
                "ejb/MatchService"
        };

        for (String path : lookupPaths) {
            try {
                MatchService service = (MatchService) ctx.lookup(path);
                if (service != null) {
                    logger.info("MatchService found at: " + path);
                    return service;
                }
            } catch (NamingException e) {
                logger.fine("MatchService not found at: " + path + " - " + e.getMessage());
            }
        }

        logger.severe("MatchService not found at any lookup path!");
        return null;
    }

    private BettingService lookupBettingService(Context ctx) {
        String[] lookupPaths = {
                "java:comp/env/ejb/BettingService",
                "java:global/ESportsBetting/BettingServiceBean!lk.esports.betting.ejb.local.BettingService",
                "java:app/ESportsBetting/BettingServiceBean!lk.esports.betting.ejb.local.BettingService",
                "java:module/BettingServiceBean!lk.esports.betting.ejb.local.BettingService",
                "ejb/BettingService"
        };

        for (String path : lookupPaths) {
            try {
                BettingService service = (BettingService) ctx.lookup(path);
                if (service != null) {
                    logger.info("BettingService found at: " + path);
                    return service;
                }
            } catch (NamingException e) {
                logger.fine("BettingService not found at: " + path + " - " + e.getMessage());
            }
        }

        logger.severe("BettingService not found at any lookup path!");
        return null;
    }
}