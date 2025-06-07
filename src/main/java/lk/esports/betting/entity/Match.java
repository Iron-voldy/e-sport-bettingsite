package lk.esports.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "matches")
@NamedQueries({
        @NamedQuery(name = "Match.findUpcoming",
                query = "SELECT m FROM Match m WHERE m.status = 'SCHEDULED' AND m.matchDate > CURRENT_TIMESTAMP ORDER BY m.matchDate"),
        @NamedQuery(name = "Match.findLive",
                query = "SELECT m FROM Match m WHERE m.status = 'LIVE' ORDER BY m.matchDate"),
        @NamedQuery(name = "Match.findCompleted",
                query = "SELECT m FROM Match m WHERE m.status = 'COMPLETED' ORDER BY m.matchDate DESC"),
        @NamedQuery(name = "Match.findByTournament",
                query = "SELECT m FROM Match m WHERE m.tournament.id = :tournamentId ORDER BY m.matchDate"),
        @NamedQuery(name = "Match.findBettable",
                query = "SELECT m FROM Match m WHERE m.bettingEnabled = true AND m.status = 'SCHEDULED' AND m.matchDate > CURRENT_TIMESTAMP ORDER BY m.matchDate")
})
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id", nullable = false)
    @NotNull(message = "Team 1 is required")
    private Team team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id", nullable = false)
    @NotNull(message = "Team 2 is required")
    private Team team2;

    @Column(name = "match_date", nullable = false)
    @NotNull(message = "Match date is required")
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type")
    private MatchType matchType = MatchType.BO3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MatchStatus status = MatchStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Column(name = "team1_score")
    private Integer team1Score = 0;

    @Column(name = "team2_score")
    private Integer team2Score = 0;

    @Column(name = "team1_odds", precision = 4, scale = 2)
    private BigDecimal team1Odds = new BigDecimal("1.50");

    @Column(name = "team2_odds", precision = 4, scale = 2)
    private BigDecimal team2Odds = new BigDecimal("2.50");

    @Column(name = "total_pool", precision = 10, scale = 2)
    private BigDecimal totalPool = BigDecimal.ZERO;

    @Column(name = "betting_enabled")
    private Boolean bettingEnabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bet> bets;

    // Enums
    public enum MatchType {
        BO1, BO3, BO5
    }

    public enum MatchStatus {
        SCHEDULED, LIVE, COMPLETED, CANCELLED
    }

    // Constructors
    public Match() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Match(Team team1, Team team2, LocalDateTime matchDate) {
        this();
        this.team1 = team1;
        this.team2 = team2;
        this.matchDate = matchDate;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean canPlaceBet() {
        return bettingEnabled && status == MatchStatus.SCHEDULED &&
                matchDate.isAfter(LocalDateTime.now());
    }

    public void startMatch() {
        this.status = MatchStatus.LIVE;
        this.bettingEnabled = false;
    }

    public void completeMatch(Team winner, int team1FinalScore, int team2FinalScore) {
        this.status = MatchStatus.COMPLETED;
        this.winnerTeam = winner;
        this.team1Score = team1FinalScore;
        this.team2Score = team2FinalScore;
        this.bettingEnabled = false;
    }

    public void updateOdds(BigDecimal newTeam1Odds, BigDecimal newTeam2Odds) {
        this.team1Odds = newTeam1Odds;
        this.team2Odds = newTeam2Odds;
    }

    public BigDecimal getOddsForTeam(Team team) {
        if (team.equals(team1)) {
            return team1Odds;
        } else if (team.equals(team2)) {
            return team2Odds;
        }
        throw new IllegalArgumentException("Team not participating in this match");
    }

    public String getFormattedMatchDate() {
        if (matchDate == null) return "";
        return matchDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getMatchTitle() {
        return team1.getTeamName() + " vs " + team2.getTeamName();
    }

    public boolean isUpcoming() {
        return status == MatchStatus.SCHEDULED && matchDate.isAfter(LocalDateTime.now());
    }

    public boolean isLive() {
        return status == MatchStatus.LIVE;
    }

    public boolean isCompleted() {
        return status == MatchStatus.COMPLETED;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Team getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(Team winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public Integer getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(Integer team1Score) {
        this.team1Score = team1Score;
    }

    public Integer getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(Integer team2Score) {
        this.team2Score = team2Score;
    }

    public BigDecimal getTeam1Odds() {
        return team1Odds;
    }

    public void setTeam1Odds(BigDecimal team1Odds) {
        this.team1Odds = team1Odds;
    }

    public BigDecimal getTeam2Odds() {
        return team2Odds;
    }

    public void setTeam2Odds(BigDecimal team2Odds) {
        this.team2Odds = team2Odds;
    }

    public BigDecimal getTotalPool() {
        return totalPool;
    }

    public void setTotalPool(BigDecimal totalPool) {
        this.totalPool = totalPool;
    }

    public Boolean getBettingEnabled() {
        return bettingEnabled;
    }

    public void setBettingEnabled(Boolean bettingEnabled) {
        this.bettingEnabled = bettingEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Bet> getBets() {
        return bets;
    }

    public void setBets(List<Bet> bets) {
        this.bets = bets;
    }

    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", team1=" + (team1 != null ? team1.getTeamName() : "null") +
                ", team2=" + (team2 != null ? team2.getTeamName() : "null") +
                ", matchDate=" + matchDate +
                ", status=" + status +
                ", team1Odds=" + team1Odds +
                ", team2Odds=" + team2Odds +
                '}';
    }
}