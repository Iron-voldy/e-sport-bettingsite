package lk.esports.betting.utils;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.ejb.UserServiceBean;
import lk.esports.betting.ejb.MatchServiceBean;
import lk.esports.betting.ejb.BettingServiceBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service locator to handle EJB lookup and provide fallback direct instances
 */
public class EJBServiceLocator {

    private static final Logger logger = Logger.getLogger(EJBServiceLocator.class.getName());

    private static volatile UserService userService;
    private static volatile MatchService matchService;
    private static volatile BettingService bettingService;

    private static final Object userServiceLock = new Object();
    private static final Object matchServiceLock = new Object();
    private static final Object bettingServiceLock = new Object();

    public static UserService getUserService() {
        if (userService == null) {
            synchronized (userServiceLock) {
                if (userService == null) {
                    userService = lookupUserService();
                }
            }
        }
        return userService;
    }

    public static MatchService getMatchService() {
        if (matchService == null) {
            synchronized (matchServiceLock) {
                if (matchService == null) {
                    matchService = lookupMatchService();
                }
            }
        }
        return matchService;
    }

    public static BettingService getBettingService() {
        if (bettingService == null) {
            synchronized (bettingServiceLock) {
                if (bettingService == null) {
                    bettingService = lookupBettingService();
                }
            }
        }
        return bettingService;
    }

    private static UserService lookupUserService() {
        try {
            // Try JNDI lookup first (for full EJB container)
            Context ctx = new InitialContext();

            String[] lookupPaths = {
                    "java:comp/env/ejb/UserService",
                    "java:global/ESportsBetting/UserServiceBean",
                    "java:app/ESportsBetting/UserServiceBean",
                    "java:module/UserServiceBean",
                    "ejb/UserService"
            };

            for (String path : lookupPaths) {
                try {
                    UserService service = (UserService) ctx.lookup(path);
                    if (service != null) {
                        logger.info("UserService found via JNDI at: " + path);
                        return service;
                    }
                } catch (NamingException e) {
                    logger.fine("UserService not found at: " + path + " - " + e.getMessage());
                }
            }

            logger.warning("JNDI lookup failed for UserService, creating direct instance");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during JNDI lookup for UserService", e);
        }

        // Fallback to direct instantiation
        try {
            UserServiceBean directInstance = new UserServiceBean();
            // Manually trigger @PostConstruct
            directInstance.init();
            logger.info("Created direct UserService instance");
            return directInstance;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating direct UserService instance", e);
            throw new RuntimeException("Failed to create UserService", e);
        }
    }

    private static MatchService lookupMatchService() {
        try {
            // Try JNDI lookup first
            Context ctx = new InitialContext();

            String[] lookupPaths = {
                    "java:comp/env/ejb/MatchService",
                    "java:global/ESportsBetting/MatchServiceBean",
                    "java:app/ESportsBetting/MatchServiceBean",
                    "java:module/MatchServiceBean",
                    "ejb/MatchService"
            };

            for (String path : lookupPaths) {
                try {
                    MatchService service = (MatchService) ctx.lookup(path);
                    if (service != null) {
                        logger.info("MatchService found via JNDI at: " + path);
                        return service;
                    }
                } catch (NamingException e) {
                    logger.fine("MatchService not found at: " + path + " - " + e.getMessage());
                }
            }

            logger.warning("JNDI lookup failed for MatchService, creating direct instance");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during JNDI lookup for MatchService", e);
        }

        // Fallback to direct instantiation
        try {
            MatchServiceBean directInstance = new MatchServiceBean();
            logger.info("Created direct MatchService instance");
            return directInstance;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating direct MatchService instance", e);
            throw new RuntimeException("Failed to create MatchService", e);
        }
    }

    private static BettingService lookupBettingService() {
        try {
            // Try JNDI lookup first
            Context ctx = new InitialContext();

            String[] lookupPaths = {
                    "java:comp/env/ejb/BettingService",
                    "java:global/ESportsBetting/BettingServiceBean",
                    "java:app/ESportsBetting/BettingServiceBean",
                    "java:module/BettingServiceBean",
                    "ejb/BettingService"
            };

            for (String path : lookupPaths) {
                try {
                    BettingService service = (BettingService) ctx.lookup(path);
                    if (service != null) {
                        logger.info("BettingService found via JNDI at: " + path);
                        return service;
                    }
                } catch (NamingException e) {
                    logger.fine("BettingService not found at: " + path + " - " + e.getMessage());
                }
            }

            logger.warning("JNDI lookup failed for BettingService, creating direct instance");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during JNDI lookup for BettingService", e);
        }

        // Fallback to direct instantiation
        try {
            BettingServiceBean directInstance = new BettingServiceBean();
            logger.info("Created direct BettingService instance");
            return directInstance;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating direct BettingService instance", e);
            throw new RuntimeException("Failed to create BettingService", e);
        }
    }

    // Clear cached services (useful for testing or redeployment)
    public static void clearCache() {
        synchronized (userServiceLock) {
            userService = null;
        }
        synchronized (matchServiceLock) {
            matchService = null;
        }
        synchronized (bettingServiceLock) {
            bettingService = null;
        }
        logger.info("EJB service cache cleared");
    }

    // Test all services
    public static boolean testServices() {
        try {
            UserService us = getUserService();
            MatchService ms = getMatchService();
            BettingService bs = getBettingService();

            boolean allAvailable = (us != null && ms != null && bs != null);
            logger.info("Service availability test - UserService: " + (us != null) +
                    ", MatchService: " + (ms != null) +
                    ", BettingService: " + (bs != null));
            return allAvailable;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error testing services", e);
            return false;
        }
    }

    // Get service status for debugging
    public static String getServiceStatus() {
        StringBuilder status = new StringBuilder();
        status.append("EJB Service Status:\n");

        try {
            status.append("- UserService: ").append(userService != null ? "LOADED" : "NOT_LOADED").append("\n");
            status.append("- MatchService: ").append(matchService != null ? "LOADED" : "NOT_LOADED").append("\n");
            status.append("- BettingService: ").append(bettingService != null ? "LOADED" : "NOT_LOADED").append("\n");
        } catch (Exception e) {
            status.append("Error getting service status: ").append(e.getMessage());
        }

        return status.toString();
    }
}