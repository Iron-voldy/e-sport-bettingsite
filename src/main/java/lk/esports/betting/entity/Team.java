package lk.esports.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teams")
@NamedQueries({
        @NamedQuery(name = "Team.findAll",
                query = "SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.teamName"),
        @NamedQuery(name = "Team.findByCode",
                query = "SELECT t FROM Team t WHERE t.teamCode = :teamCode"),
        @NamedQuery(name = "Team.findByCountry",
                query = "SELECT t FROM Team t WHERE t.country = :country AND t.isActive = true"),
        @NamedQuery(name = "Team.findTopTeams",
                query = "SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.winRate DESC")
})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_name", nullable = false, length = 100)
    @NotBlank(message = "Team name is required")
    private String teamName;

    @Column(name = "team_code", unique = true, nullable = false, length = 10)
    @NotBlank(message = "Team code is required")
    private String teamCode;

    @Column(length = 50)
    private String country;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "win_rate", precision = 5, scale = 2)
    private BigDecimal winRate = BigDecimal.ZERO;

    @Column(name = "total_matches")
    private Integer totalMatches = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "team1", fetch = FetchType.LAZY)
    private List<Match> homeMatches;

    @OneToMany(mappedBy = "team2", fetch = FetchType.LAZY)
    private List<Match> awayMatches;

    @OneToMany(mappedBy = "selectedTeam", fetch = FetchType.LAZY)
    private List<Bet> betsPlaced;

    // Constructors
    public Team() {
        this.createdAt = LocalDateTime.now();
    }

    public Team(String teamName, String teamCode, String country) {
        this();
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.country = country;
    }

    // Business methods
    public void updateWinRate(int wins, int totalMatches) {
        if (totalMatches > 0) {
            this.winRate = BigDecimal.valueOf(wins * 100.0 / totalMatches)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            this.totalMatches = totalMatches;
        }
    }

    public boolean isValidForMatch() {
        return isActive != null && isActive;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getWinRate() {
        return winRate;
    }

    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }

    public Integer getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(Integer totalMatches) {
        this.totalMatches = totalMatches;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Match> getHomeMatches() {
        return homeMatches;
    }

    public void setHomeMatches(List<Match> homeMatches) {
        this.homeMatches = homeMatches;
    }

    public List<Match> getAwayMatches() {
        return awayMatches;
    }

    public void setAwayMatches(List<Match> awayMatches) {
        this.awayMatches = awayMatches;
    }

    public List<Bet> getBetsPlaced() {
        return betsPlaced;
    }

    public void setBetsPlaced(List<Bet> betsPlaced) {
        this.betsPlaced = betsPlaced;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", teamName='" + teamName + '\'' +
                ", teamCode='" + teamCode + '\'' +
                ", country='" + country + '\'' +
                ", winRate=" + winRate +
                ", totalMatches=" + totalMatches +
                '}';
    }
}