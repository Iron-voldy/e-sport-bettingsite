package lk.esports.betting.ejb.local;

import lk.esports.betting.entity.User;
import lk.esports.betting.entity.Transaction;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

@Local
public interface UserService {

    // User Authentication and Registration
    User registerUser(String email, String username, String password, String fullName, String phone);
    User authenticateUser(String email, String password);
    boolean isEmailExists(String email);
    boolean isUsernameExists(String username);

    // User Management
    User findUserById(Long userId);
    User findUserByEmail(String email);
    User findUserByUsername(String username);
    void updateUser(User user);
    void deactivateUser(Long userId);
    void activateUser(Long userId);

    // Wallet Management
    BigDecimal getUserBalance(Long userId);
    void addFunds(Long userId, BigDecimal amount, String description);
    boolean withdrawFunds(Long userId, BigDecimal amount, String description);
    boolean deductFunds(Long userId, BigDecimal amount, String description);
    void refundFunds(Long userId, BigDecimal amount, String description);

    // Transaction History
    List<Transaction> getUserTransactions(Long userId);
    List<Transaction> getUserTransactionsByType(Long userId, Transaction.TransactionType type);
    Transaction createTransaction(Long userId, Transaction.TransactionType type,
                                  BigDecimal amount, String description, Long referenceId);

    // User Statistics
    BigDecimal getTotalBetAmount(Long userId);
    BigDecimal getTotalWinnings(Long userId);
    int getTotalBetsPlaced(Long userId);
    int getWonBetsCount(Long userId);
    double getWinRate(Long userId);

    // Password Management
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    String hashPassword(String password);
    boolean verifyPassword(String password, String hashedPassword);

    // User Profile
    void updateProfile(Long userId, String fullName, String phone);
    List<User> getActiveUsers();
    List<User> getAllUsers();

    // Account Validation
    boolean canPlaceBet(Long userId, BigDecimal amount);
    boolean isAccountActive(Long userId);
}