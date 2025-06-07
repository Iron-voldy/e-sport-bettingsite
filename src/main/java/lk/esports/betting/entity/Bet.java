package lk.esports.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "bets")
@NamedQueries({
        @NamedQuery(name = "Bet.findByUser",
                query = "SELECT b FROM Bet b WHERE b.user.id = :userId ORDER BY b.betPlacedAt DESC"),
        @NamedQuery(name = "Bet.findByMatch",
                query = "SELECT b FROM Bet b WHERE b.match.id = :matchId ORDER BY b.betPlacedAt DESC"),
        @NamedQuery(name = "Bet.findByStatus",
                query = "SELECT b FROM Bet b WHERE b.status = :status ORDER BY b.betPlacedAt DESC"),
        @NamedQuery(name = "Bet.findPendingBets",
                query = "SELECT b FROM Bet b WHERE b.status = 'PENDING' ORDER BY b.betPlacedAt"),
        @NamedQuery(name = "Bet.findUserWinnings",
                query = "SELECT b FROM Bet b WHERE b.user.id = :userId AND b.status = 'WON' ORDER BY b.resultProcessedAt DESC"),
        @NamedQuery(name = "Bet.findUserBetsByMatch",
                query = "SELECT b FROM Bet b WHERE b.user.id = :userId AND b.match.id = :matchId"),
        @NamedQuery(name = "Bet.getTotalBetAmount",
                query = "SELECT SUM(b.betAmount) FROM Bet b WHERE b.match.id = :matchId"),
        @NamedQuery(name = "Bet.getTotalBetAmountForTeam",
                query = "SELECT SUM(b.betAmount) FROM Bet b WHERE b.match.id = :matchId AND b.selectedTeam.id = :teamId")
})
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", nullable = false)
    @NotNull(message = "Match is required")
    private Match match;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "selected_team_id", nullable = false)
    @NotNull(message = "Selected team is required")
    private Team selectedTeam;

    @Column(name = "bet_amount", nullable = false, precision = 8, scale = 2)
    @NotNull(message = "Bet amount is required")
    @DecimalMin(value = "1.00", message = "Minimum bet amount is $1.00")
    private BigDecimal betAmount;

    @Column(name = "odds_at_bet", nullable = false, precision = 4, scale = 2)
    @NotNull(message = "Odds are required")
    private BigDecimal oddsAtBet;

    @Column(name = "potential_winnings", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Potential winnings are required")
    private BigDecimal potentialWinnings;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BetStatus status = BetStatus.PENDING;

    @Column(name = "bet_placed_at")
    private LocalDateTime betPlacedAt;

    @Column(name = "result_processed_at")
    private LocalDateTime resultProcessedAt;

    // Enums
    public enum BetStatus {
        PENDING, WON, LOST, CANCELLED
    }

    // Constructors
    public Bet() {
        this.betPlacedAt = LocalDateTime.now();
    }

    public Bet(User user, Match match, Team selectedTeam, BigDecimal betAmount, BigDecimal oddsAtBet) {
        this();
        this.user = user;
        this.match = match;
        this.selectedTeam = selectedTeam;
        this.betAmount = betAmount;
        this.oddsAtBet = oddsAtBet;
        this.potentialWinnings = calculatePotentialWinnings();
    }

    // Business methods
    public BigDecimal calculatePotentialWinnings() {
        if (betAmount != null && oddsAtBet != null) {
            return betAmount.multiply(oddsAtBet).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public void markAsWon() {
        this.status = BetStatus.WON;
        this.resultProcessedAt = LocalDateTime.now();
    }

    public void markAsLost() {
        this.status = BetStatus.LOST;
        this.resultProcessedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = BetStatus.CANCELLED;
        this.resultProcessedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == BetStatus.PENDING;
    }

    public boolean isWon() {
        return status == BetStatus.WON;
    }

    public boolean isLost() {
        return status == BetStatus.LOST;
    }

    public boolean isCancelled() {
        return status == BetStatus.CANCELLED;
    }

    public String getFormattedBetAmount() {
        return String.format("$%.2f", betAmount);
    }

    public String getFormattedPotentialWinnings() {
        return String.format("$%.2f", potentialWinnings);
    }

    public String getFormattedOdds() {
        return String.format("%.2f", oddsAtBet);
    }

    public String getFormattedBetPlacedAt() {
        if (betPlacedAt == null) return "";
        return betPlacedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getFormattedResultProcessedAt() {
        if (resultProcessedAt == null) return "";
        return resultProcessedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case PENDING:
                return "bg-warning";
            case WON:
                return "bg-success";
            case LOST:
                return "bg-danger";
            case CANCELLED:
                return "bg-secondary";
            default:
                return "bg-light";
        }
    }

    public String getStatusDisplayText() {
        switch (status) {
            case PENDING:
                return "Pending";
            case WON:
                return "Won";
            case LOST:
                return "Lost";
            case CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    public boolean canBeCancelled() {
        return status == BetStatus.PENDING && match.canPlaceBet();
    }

    public BigDecimal getActualWinnings() {
        if (isWon()) {
            return potentialWinnings.subtract(betAmount);
        }
        return BigDecimal.ZERO;
    }

    // Validation methods
    public boolean isValidBet() {
        return user != null &&
                match != null &&
                selectedTeam != null &&
                betAmount != null &&
                betAmount.compareTo(BigDecimal.ZERO) > 0 &&
                oddsAtBet != null &&
                oddsAtBet.compareTo(BigDecimal.ZERO) > 0 &&
                match.canPlaceBet() &&
                (selectedTeam.equals(match.getTeam1()) || selectedTeam.equals(match.getTeam2()));
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

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Team getSelectedTeam() {
        return selectedTeam;
    }

    public void setSelectedTeam(Team selectedTeam) {
        this.selectedTeam = selectedTeam;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
        if (this.oddsAtBet != null) {
            this.potentialWinnings = calculatePotentialWinnings();
        }
    }

    public BigDecimal getOddsAtBet() {
        return oddsAtBet;
    }

    public void setOddsAtBet(BigDecimal oddsAtBet) {
        this.oddsAtBet = oddsAtBet;
        if (this.betAmount != null) {
            this.potentialWinnings = calculatePotentialWinnings();
        }
    }

    public BigDecimal getPotentialWinnings() {
        return potentialWinnings;
    }

    public void setPotentialWinnings(BigDecimal potentialWinnings) {
        this.potentialWinnings = potentialWinnings;
    }

    public BetStatus getStatus() {
        return status;
    }

    public void setStatus(BetStatus status) {
        this.status = status;
    }

    public LocalDateTime getBetPlacedAt() {
        return betPlacedAt;
    }

    public void setBetPlacedAt(LocalDateTime betPlacedAt) {
        this.betPlacedAt = betPlacedAt;
    }

    public LocalDateTime getResultProcessedAt() {
        return resultProcessedAt;
    }

    public void setResultProcessedAt(LocalDateTime resultProcessedAt) {
        this.resultProcessedAt = resultProcessedAt;
    }

    @Override
    public String toString() {
        return "Bet{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", match=" + (match != null ? match.getMatchTitle() : "null") +
                ", selectedTeam=" + (selectedTeam != null ? selectedTeam.getTeamName() : "null") +
                ", betAmount=" + betAmount +
                ", oddsAtBet=" + oddsAtBet +
                ", potentialWinnings=" + potentialWinnings +
                ", status=" + status +
                '}';
    }
}