package lk.esports.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "transactions")
@NamedQueries({
        @NamedQuery(name = "Transaction.findByUser",
                query = "SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC"),
        @NamedQuery(name = "Transaction.findByType",
                query = "SELECT t FROM Transaction t WHERE t.transactionType = :type ORDER BY t.createdAt DESC"),
        @NamedQuery(name = "Transaction.findByStatus",
                query = "SELECT t FROM Transaction t WHERE t.status = :status ORDER BY t.createdAt DESC"),
        @NamedQuery(name = "Transaction.findUserDeposits",
                query = "SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = 'DEPOSIT' ORDER BY t.createdAt DESC"),
        @NamedQuery(name = "Transaction.findUserWithdrawals",
                query = "SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = 'WITHDRAWAL' ORDER BY t.createdAt DESC"),
        @NamedQuery(name = "Transaction.findUserBetTransactions",
                query = "SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionType IN ('BET_PLACED', 'WINNINGS') ORDER BY t.createdAt DESC"),
        @NamedQuery(name = "Transaction.getUserBalance",
                query = "SELECT SUM(CASE WHEN t.transactionType IN ('DEPOSIT', 'WINNINGS', 'REFUND') THEN t.amount ELSE -t.amount END) FROM Transaction t WHERE t.user.id = :userId AND t.status = 'COMPLETED'")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_id")
    private Long referenceId; // Can reference bet_id or other entities

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Enums
    public enum TransactionType {
        DEPOSIT("Deposit"),
        WITHDRAWAL("Withdrawal"),
        BET_PLACED("Bet Placed"),
        WINNINGS("Winnings"),
        REFUND("Refund");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransactionStatus {
        PENDING("Pending"),
        COMPLETED("Completed"),
        FAILED("Failed");

        private final String displayName;

        TransactionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Transaction() {
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(User user, TransactionType transactionType, BigDecimal amount, String description) {
        this();
        this.user = user;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
    }

    public Transaction(User user, TransactionType transactionType, BigDecimal amount, String description, Long referenceId) {
        this(user, transactionType, amount, description);
        this.referenceId = referenceId;
    }

    // Business methods
    public boolean isCredit() {
        return transactionType == TransactionType.DEPOSIT ||
                transactionType == TransactionType.WINNINGS ||
                transactionType == TransactionType.REFUND;
    }

    public boolean isDebit() {
        return transactionType == TransactionType.WITHDRAWAL ||
                transactionType == TransactionType.BET_PLACED;
    }

    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
    }

    public void markAsPending() {
        this.status = TransactionStatus.PENDING;
    }

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    public String getFormattedAmount() {
        String sign = isCredit() ? "+" : "-";
        return String.format("%s$%.2f", sign, amount);
    }

    public String getFormattedAmountWithoutSign() {
        return String.format("$%.2f", amount);
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getAmountColorClass() {
        if (isCredit()) {
            return "text-success";
        } else if (isDebit()) {
            return "text-danger";
        }
        return "text-muted";
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case PENDING:
                return "bg-warning";
            case COMPLETED:
                return "bg-success";
            case FAILED:
                return "bg-danger";
            default:
                return "bg-secondary";
        }
    }

    public String getTypeIconClass() {
        switch (transactionType) {
            case DEPOSIT:
                return "bi-arrow-down-circle text-success";
            case WITHDRAWAL:
                return "bi-arrow-up-circle text-danger";
            case BET_PLACED:
                return "bi-dice-3 text-primary";
            case WINNINGS:
                return "bi-trophy text-warning";
            case REFUND:
                return "bi-arrow-clockwise text-info";
            default:
                return "bi-circle text-muted";
        }
    }

    public String getShortDescription() {
        if (description != null && description.length() > 50) {
            return description.substring(0, 47) + "...";
        }
        return description;
    }

    // Static factory methods
    public static Transaction createDeposit(User user, BigDecimal amount, String description) {
        return new Transaction(user, TransactionType.DEPOSIT, amount, description);
    }

    public static Transaction createWithdrawal(User user, BigDecimal amount, String description) {
        return new Transaction(user, TransactionType.WITHDRAWAL, amount, description);
    }

    public static Transaction createBetPlaced(User user, BigDecimal amount, Long betId) {
        String description = String.format("Bet placed for match (Bet ID: %d)", betId);
        return new Transaction(user, TransactionType.BET_PLACED, amount, description, betId);
    }

    public static Transaction createWinnings(User user, BigDecimal amount, Long betId) {
        String description = String.format("Winnings from bet (Bet ID: %d)", betId);
        return new Transaction(user, TransactionType.WINNINGS, amount, description, betId);
    }

    public static Transaction createRefund(User user, BigDecimal amount, Long betId) {
        String description = String.format("Refund for cancelled bet (Bet ID: %d)", betId);
        return new Transaction(user, TransactionType.REFUND, amount, description, betId);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}