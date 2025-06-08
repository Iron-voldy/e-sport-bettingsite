package lk.esports.betting.ejb;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.entity.User;
import lk.esports.betting.entity.Transaction;
import lk.esports.betting.utils.DatabaseUtil;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.PostConstruct;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class UserServiceBean implements UserService {

    private static final Logger logger = Logger.getLogger(UserServiceBean.class.getName());

    @PersistenceContext(unitName = "esportsPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        logger.info("UserServiceBean initialized");
    }

    private EntityManager getEntityManager() {
        if (em == null) {
            logger.warning("EntityManager is null, creating new one using DatabaseUtil");
            return DatabaseUtil.createEntityManager();
        }
        return em;
    }

    @Override
    public User registerUser(String email, String username, String password, String fullName, String phone) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            // Check if we need to manage transactions manually
            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            // Check if email or username already exists
            if (isEmailExists(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            if (isUsernameExists(username)) {
                throw new IllegalArgumentException("Username already exists");
            }

            // Hash password
            String hashedPassword = hashPassword(password);

            // Create new user
            User user = new User(email, username, hashedPassword, fullName);
            user.setPhone(phone);
            user.setWalletBalance(BigDecimal.ZERO);
            user.setIsActive(true);

            // Persist user
            entityManager.persist(user);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();

                // Refresh to get the ID
                entityManager.refresh(user);
            } else {
                entityManager.flush(); // Force immediate write to get ID
            }

            logger.info("New user registered successfully: " + username + " (" + email + ")");
            return user;

        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error registering user: " + email, e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public User authenticateUser(String email, String password) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();

            User user = findUserByEmailInternal(entityManager, email);
            if (user != null && user.getIsActive() && verifyPassword(password, user.getPassword())) {
                logger.info("User authenticated successfully: " + email);
                return user;
            }
            logger.warning("Authentication failed for: " + email);
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error authenticating user: " + email, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking email existence: " + email, e);
            return false;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean isUsernameExists(String username) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking username existence: " + username, e);
            return false;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public User findUserById(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            return entityManager.find(User.class, userId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding user by ID: " + userId, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public User findUserByEmail(String email) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            return findUserByEmailInternal(entityManager, email);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding user by email: " + email, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    private User findUserByEmailInternal(EntityManager entityManager, String email) {
        try {
            TypedQuery<User> query = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User findUserByUsername(String username) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<User> query = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding user by username: " + username, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateUser(User user) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            user.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(user);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            }

            logger.info("User updated: " + user.getUsername());
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error updating user: " + user.getUsername(), e);
            throw new RuntimeException("Failed to update user", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void deactivateUser(Long userId) {
        try {
            User user = findUserById(userId);
            if (user != null) {
                user.setIsActive(false);
                updateUser(user);
                logger.info("User deactivated: " + user.getUsername());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deactivating user: " + userId, e);
            throw new RuntimeException("Failed to deactivate user", e);
        }
    }

    @Override
    public void activateUser(Long userId) {
        try {
            User user = findUserById(userId);
            if (user != null) {
                user.setIsActive(true);
                updateUser(user);
                logger.info("User activated: " + user.getUsername());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error activating user: " + userId, e);
            throw new RuntimeException("Failed to activate user", e);
        }
    }

    @Override
    public BigDecimal getUserBalance(Long userId) {
        User user = findUserById(userId);
        return user != null ? user.getWalletBalance() : BigDecimal.ZERO;
    }

    @Override
    public void addFunds(Long userId, BigDecimal amount, String description) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            User user = entityManager.find(User.class, userId);
            if (user != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                user.addBalance(amount);
                entityManager.merge(user);

                // Create transaction record
                Transaction txn = new Transaction(user, Transaction.TransactionType.DEPOSIT, amount, description);
                entityManager.persist(txn);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Funds added to user " + user.getUsername() + ": $" + amount);
            } else {
                if (useLocalTransaction) {
                    entityManager.getTransaction().rollback();
                }
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error adding funds for user: " + userId, e);
            throw new RuntimeException("Failed to add funds", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean withdrawFunds(Long userId, BigDecimal amount, String description) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            User user = entityManager.find(User.class, userId);
            if (user != null && user.canPlaceBet(amount)) {
                user.deductBalance(amount);
                entityManager.merge(user);

                // Create transaction record
                Transaction txn = new Transaction(user, Transaction.TransactionType.WITHDRAWAL, amount, description);
                entityManager.persist(txn);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Funds withdrawn from user " + user.getUsername() + ": $" + amount);
                return true;
            } else {
                if (useLocalTransaction) {
                    entityManager.getTransaction().rollback();
                }
                return false;
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error withdrawing funds for user: " + userId, e);
            return false;
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean deductFunds(Long userId, BigDecimal amount, String description) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            User user = entityManager.find(User.class, userId);
            if (user != null && user.canPlaceBet(amount)) {
                user.deductBalance(amount);
                entityManager.merge(user);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Funds deducted from user " + user.getUsername() + ": $" + amount);
                return true;
            } else {
                if (useLocalTransaction) {
                    entityManager.getTransaction().rollback();
                }
                return false;
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error deducting funds for user: " + userId, e);
            return false;
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void refundFunds(Long userId, BigDecimal amount, String description) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            User user = entityManager.find(User.class, userId);
            if (user != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                user.addBalance(amount);
                entityManager.merge(user);

                // Create transaction record
                Transaction txn = new Transaction(user, Transaction.TransactionType.REFUND, amount, description);
                entityManager.persist(txn);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Funds refunded to user " + user.getUsername() + ": $" + amount);
            } else {
                if (useLocalTransaction) {
                    entityManager.getTransaction().rollback();
                }
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error refunding funds for user: " + userId, e);
            throw new RuntimeException("Failed to refund funds", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Transaction> getUserTransactions(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Transaction> query = entityManager.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC",
                    Transaction.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user transactions: " + userId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Transaction> getUserTransactionsByType(Long userId, Transaction.TransactionType type) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Transaction> query = entityManager.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = :type ORDER BY t.createdAt DESC",
                    Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user transactions by type: " + userId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Transaction createTransaction(Long userId, Transaction.TransactionType type,
                                         BigDecimal amount, String description, Long referenceId) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            User user = findUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
            }

            if (useLocalTransaction && !entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            Transaction txn = new Transaction(user, type, amount, description, referenceId);
            entityManager.persist(txn);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            }

            return txn;
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error creating transaction for user: " + userId, e);
            throw new RuntimeException("Failed to create transaction", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public BigDecimal getTotalBetAmount(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<BigDecimal> query = entityManager.createQuery(
                    "SELECT COALESCE(SUM(b.betAmount), 0) FROM Bet b WHERE b.user.id = :userId",
                    BigDecimal.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total bet amount for user: " + userId, e);
            return BigDecimal.ZERO;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public BigDecimal getTotalWinnings(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<BigDecimal> query = entityManager.createQuery(
                    "SELECT COALESCE(SUM(b.potentialWinnings - b.betAmount), 0) FROM Bet b WHERE b.user.id = :userId AND b.status = 'WON'",
                    BigDecimal.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total winnings for user: " + userId, e);
            return BigDecimal.ZERO;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public int getTotalBetsPlaced(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.user.id = :userId",
                    Long.class);
            query.setParameter("userId", userId);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total bets count for user: " + userId, e);
            return 0;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public int getWonBetsCount(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.user.id = :userId AND b.status = 'WON'",
                    Long.class);
            query.setParameter("userId", userId);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting won bets count for user: " + userId, e);
            return 0;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public double getWinRate(Long userId) {
        int totalBets = getTotalBetsPlaced(userId);
        if (totalBets == 0) return 0.0;

        int wonBets = getWonBetsCount(userId);
        return (double) wonBets / totalBets * 100.0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            User user = findUserById(userId);
            if (user != null && verifyPassword(oldPassword, user.getPassword())) {
                user.setPassword(hashPassword(newPassword));
                updateUser(user);
                logger.info("Password changed for user: " + user.getUsername());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error changing password for user: " + userId, e);
            return false;
        }
    }

    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Password verification failed", e);
            return false;
        }
    }

    @Override
    public void updateProfile(Long userId, String fullName, String phone) {
        try {
            User user = findUserById(userId);
            if (user != null) {
                user.setFullName(fullName);
                user.setPhone(phone);
                updateUser(user);
                logger.info("Profile updated for user: " + user.getUsername());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating profile for user: " + userId, e);
            throw new RuntimeException("Failed to update profile", e);
        }
    }

    @Override
    public List<User> getActiveUsers() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<User> query = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC",
                    User.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active users", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<User> getAllUsers() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u ORDER BY u.createdAt DESC", User.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all users", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean canPlaceBet(Long userId, BigDecimal amount) {
        User user = findUserById(userId);
        return user != null && user.canPlaceBet(amount);
    }

    @Override
    public boolean isAccountActive(Long userId) {
        User user = findUserById(userId);
        return user != null && user.getIsActive();
    }
}