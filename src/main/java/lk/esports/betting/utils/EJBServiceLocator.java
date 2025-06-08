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

    private static UserService userService;
    private static MatchService matchService;
    private static BettingService bettingService;

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
            Context ctx = new InitialContext();

            // Try JNDI lookup first
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
                    logger.fine("UserService not found at: " + path);
                }
            }

            // If JNDI lookup fails, create direct instance
            logger.warning("JNDI lookup failed for UserService, creating direct instance");
            UserServiceBean directInstance = new UserServiceBean();

            // Initialize the EntityManager manually since we're not using EJB container
            try {
                // The direct instance will use DatabaseUtil for persistence operations
                logger.info("Created direct UserService instance");
                return directInstance;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error initializing direct UserService instance", e);
                return directInstance; // Return anyway, errors will be handled in service methods
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error looking up UserService", e);
            // Return direct instance as fallback
            return new UserServiceBean();
        }
    }

    private static MatchService lookupMatchService() {
        try {
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
                    logger.fine("MatchService not found at: " + path);
                }
            }

            logger.warning("JNDI lookup failed for MatchService, creating direct instance");
            MatchServiceBean directInstance = new MatchServiceBean();
            logger.info("Created direct MatchService instance");
            return directInstance;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error looking up MatchService", e);
            return new MatchServiceBean();
        }
    }

    private static BettingService lookupBettingService() {
        try {
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
                    logger.fine("BettingService not found at: " + path);
                }
            }

            logger.warning("JNDI lookup failed for BettingService, creating direct instance");
            BettingServiceBean directInstance = new BettingServiceBean();
            logger.info("Created direct BettingService instance");
            return directInstance;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error looking up BettingService", e);
            return new BettingServiceBean();
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
    }
}