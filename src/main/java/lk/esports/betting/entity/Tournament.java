package lk.esports.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "tournaments")
@NamedQueries({
        @NamedQuery(name = "Tournament.findAll",
                query = "SELECT t FROM Tournament t ORDER BY t.startDate DESC"),
        @NamedQuery(name = "Tournament.findActive",
                query = "SELECT t FROM Tournament t WHERE t.status = 'ONGOING' ORDER BY t.startDate"),
        @NamedQuery(name = "Tournament.findUpcoming",
                query = "SELECT t FROM Tournament t WHERE t.status = 'UPCOMING' ORDER BY t.startDate"),
        @NamedQuery(name = "Tournament.findByType",
                query = "SELECT t FROM Tournament t WHERE t.tournamentType = :type ORDER BY t.startDate DESC"),
        @NamedQuery(name = "Tournament.findByStatus",
                query = "SELECT t FROM Tournament t WHERE t.status = :status ORDER BY t.startDate DESC")
})
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_name", nullable = false, length = 100)
    @NotBlank(message = "Tournament name is required")
    private String tournamentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_type")
    private TournamentType tournamentType = TournamentType.REGULAR;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "prize_pool", precision = 12, scale = 2)
    private BigDecimal prizePool;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TournamentStatus status = TournamentStatus.UPCOMING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matches;

    // Enums
    public enum TournamentType {
        REGULAR, PLAYOFF, CHAMPIONSHIP
    }

    public enum TournamentStatus {
        UPCOMING, ONGOING, COMPLETED
    }

    // Constructors
    public Tournament() {
        this.createdAt = LocalDateTime.now();
    }

    public Tournament(String tournamentName, TournamentType tournamentType, LocalDate startDate, LocalDate endDate) {
        this();
        this.tournamentName = tournamentName;
        this.tournamentType = tournamentType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Business methods
    public boolean isActive() {
        return status == TournamentStatus.ONGOING;
    }

    public boolean isUpcoming() {
        return status == TournamentStatus.UPCOMING;
    }

    public boolean isCompleted() {
        return status == TournamentStatus.COMPLETED;
    }

    public void startTournament() {
        this.status = TournamentStatus.ONGOING;
    }

    public void completeTournament() {
        this.status = TournamentStatus.COMPLETED;
    }

    public String getFormattedStartDate() {
        if (startDate == null) return "";
        return startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    public String getFormattedEndDate() {
        if (endDate == null) return "";
        return endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    public String getFormattedPrizePool() {
        if (prizePool == null) return "$0";
        return String.format("$%,.2f", prizePool);
    }

    public String getDateRange() {
        if (startDate == null && endDate == null) return "TBD";
        if (startDate != null && endDate != null) {
            return getFormattedStartDate() + " - " + getFormattedEndDate();
        }
        return startDate != null ? getFormattedStartDate() : getFormattedEndDate();
    }

    public int getTotalMatches() {
        return matches != null ? matches.size() : 0;
    }

    public long getCompletedMatches() {
        if (matches == null) return 0;
        return matches.stream()
                .filter(match -> match.getStatus() == Match.MatchStatus.COMPLETED)
                .count();
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case UPCOMING:
                return "bg-warning";
            case ONGOING:
                return "bg-success";
            case COMPLETED:
                return "bg-secondary";
            default:
                return "bg-light";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(BigDecimal prizePool) {
        this.prizePool = prizePool;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + id +
                ", tournamentName='" + tournamentName + '\'' +
                ", tournamentType=" + tournamentType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", prizePool=" + prizePool +
                '}';
    }
}