package lk.esports.betting.ejb;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Team;
import lk.esports.betting.entity.Tournament;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class MatchServiceBean implements MatchService {

    private static final Logger logger = Logger.getLogger(MatchServiceBean.class.getName());

    @PersistenceContext(unitName = "esportsPU")
    private EntityManager em;

    // Match Management
    @Override
    public Match createMatch(Long tournamentId, Long team1Id, Long team2Id, LocalDateTime matchDate,
                             Match.MatchType matchType) {
        try {
            if (!isValidMatchup(team1Id, team2Id)) {
                throw new IllegalArgumentException("Invalid team matchup");
            }

            Tournament tournament = tournamentId != null ? findTournamentById(tournamentId) : null;
            Team team1 = findTeamById(team1Id);
            Team team2 = findTeamById(team2Id);

            if (team1 == null || team2 == null) {
                throw new IllegalArgumentException("Invalid teams");
            }

            Match match = new Match(team1, team2, matchDate);
            match.setTournament(tournament);
            match.setMatchType(matchType);
            match.setStatus(Match.MatchStatus.SCHEDULED);
            match.setBettingEnabled(true);

            em.persist(match);
            em.flush();

            logger.info("Match created: " + match.getMatchTitle());
            return match;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating match", e);
            throw new RuntimeException("Failed to create match", e);
        }
    }

    @Override
    public Match findMatchById(Long matchId) {
        try {
            return em.find(Match.class, matchId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding match by ID: " + matchId, e);
            return null;
        }
    }

    @Override
    public void updateMatch(Match match) {
        try {
            match.setUpdatedAt(LocalDateTime.now());
            em.merge(match);
            logger.info("Match updated: " + match.getMatchTitle());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating match: " + match.getId(), e);
            throw new RuntimeException("Failed to update match", e);
        }
    }

    @Override
    public void deleteMatch(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            if (match != null) {
                em.remove(match);
                logger.info("Match deleted: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting match: " + matchId, e);
            throw new RuntimeException("Failed to delete match", e);
        }
    }

    // Match Queries
    @Override
    public List<Match> getAllMatches() {
        try {
            TypedQuery<Match> query = em.createQuery("SELECT m FROM Match m ORDER BY m.matchDate DESC", Match.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all matches", e);
            return List.of();
        }
    }

    @Override
    public List<Match> getUpcomingMatches() {
        try {
            return em.createNamedQuery("Match.findUpcoming", Match.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming matches", e);
            return List.of();
        }
    }

    @Override
    public List<Match> getLiveMatches() {
        try {
            return em.createNamedQuery("Match.findLive", Match.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting live matches", e);
            return List.of();
        }
    }

    @Override
    public List<Match> getCompletedMatches() {
        try {
            return em.createNamedQuery("Match.findCompleted", Match.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting completed matches", e);
            return List.of();
        }
    }

    @Override
    public List<Match> getMatchesByTournament(Long tournamentId) {
        try {
            return em.createNamedQuery("Match.findByTournament", Match.class)
                    .setParameter("tournamentId", tournamentId)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by tournament: " + tournamentId, e);
            return List.of();
        }
    }

    @Override
    public List<Match> getBettableMatches() {
        try {
            return em.createNamedQuery("Match.findBettable", Match.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting bettable matches", e);
            return List.of();
        }
    }

    @Override
    public List<Match> getMatchesByTeam(Long teamId) {
        try {
            TypedQuery<Match> query = em.createQuery(
                    "SELECT m FROM Match m WHERE m.team1.id = :teamId OR m.team2.id = :teamId ORDER BY m.matchDate DESC",
                    Match.class);
            query.setParameter("teamId", teamId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by team: " + teamId, e);
            return List.of();
        }
    }

    @Override
    public List<Match> getMatchesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            TypedQuery<Match> query = em.createQuery(
                    "SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate ORDER BY m.matchDate",
                    Match.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by date range", e);
            return List.of();
        }
    }

    // Match Status Management
    @Override
    public void startMatch(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            if (match != null && match.getStatus() == Match.MatchStatus.SCHEDULED) {
                match.startMatch();
                updateMatch(match);
                logger.info("Match started: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting match: " + matchId, e);
            throw new RuntimeException("Failed to start match", e);
        }
    }

    @Override
    public void completeMatch(Long matchId, Long winnerTeamId, int team1Score, int team2Score) {
        try {
            Match match = findMatchById(matchId);
            Team winnerTeam = findTeamById(winnerTeamId);

            if (match != null && winnerTeam != null) {
                match.completeMatch(winnerTeam, team1Score, team2Score);
                updateMatch(match);

                // Update team statistics
                updateTeamStats(match.getTeam1().getId());
                updateTeamStats(match.getTeam2().getId());

                logger.info("Match completed: " + match.getMatchTitle() + ", Winner: " + winnerTeam.getTeamName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error completing match: " + matchId, e);
            throw new RuntimeException("Failed to complete match", e);
        }
    }

    @Override
    public void cancelMatch(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            if (match != null) {
                match.setStatus(Match.MatchStatus.CANCELLED);
                match.setBettingEnabled(false);
                updateMatch(match);
                logger.info("Match cancelled: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error cancelling match: " + matchId, e);
            throw new RuntimeException("Failed to cancel match", e);
        }
    }

    @Override
    public void enableBetting(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            if (match != null && match.canPlaceBet()) {
                match.setBettingEnabled(true);
                updateMatch(match);
                logger.info("Betting enabled for match: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error enabling betting for match: " + matchId, e);
            throw new RuntimeException("Failed to enable betting", e);
        }
    }

    @Override
    public void disableBetting(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            if (match != null) {
                match.setBettingEnabled(false);
                updateMatch(match);
                logger.info("Betting disabled for match: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error disabling betting for match: " + matchId, e);
            throw new RuntimeException("Failed to disable betting", e);
        }
    }

    // Odds Management
    @Override
    public void updateOdds(Long matchId, BigDecimal team1Odds, BigDecimal team2Odds) {
        try {
            Match match = findMatchById(matchId);
            if (match != null) {
                match.updateOdds(team1Odds, team2Odds);
                updateMatch(match);
                logger.info("Odds updated for match: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating odds for match: " + matchId, e);
            throw new RuntimeException("Failed to update odds", e);
        }
    }

    @Override
    public BigDecimal calculateDynamicOdds(Long matchId, Long teamId) {
        try {
            BigDecimal totalPool = getTotalBetPool(matchId);
            BigDecimal teamPool = getTeamBetPool(matchId, teamId);

            if (totalPool.compareTo(BigDecimal.ZERO) == 0 || teamPool.compareTo(BigDecimal.ZERO) == 0) {
                return new BigDecimal("2.00"); // Default odds
            }

            // Simple dynamic odds calculation: total_pool / team_pool * margin
            BigDecimal margin = new BigDecimal("0.95"); // 5% house edge
            return totalPool.divide(teamPool, 2, BigDecimal.ROUND_HALF_UP).multiply(margin);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating dynamic odds", e);
            return new BigDecimal("2.00");
        }
    }

    @Override
    public void recalculateOdds(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            if (match != null) {
                BigDecimal team1Odds = calculateDynamicOdds(matchId, match.getTeam1().getId());
                BigDecimal team2Odds = calculateDynamicOdds(matchId, match.getTeam2().getId());
                updateOdds(matchId, team1Odds, team2Odds);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error recalculating odds for match: " + matchId, e);
        }
    }

    // Team Management
    @Override
    public Team createTeam(String teamName, String teamCode, String country, String logoUrl, String description) {
        try {
            // Check if team code already exists
            Team existingTeam = findTeamByCode(teamCode);
            if (existingTeam != null) {
                throw new IllegalArgumentException("Team code already exists: " + teamCode);
            }

            Team team = new Team(teamName, teamCode, country);
            team.setLogoUrl(logoUrl);
            team.setDescription(description);

            em.persist(team);
            em.flush();

            logger.info("Team created: " + teamName + " (" + teamCode + ")");
            return team;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating team: " + teamName, e);
            throw new RuntimeException("Failed to create team", e);
        }
    }

    @Override
    public Team findTeamById(Long teamId) {
        try {
            return em.find(Team.class, teamId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding team by ID: " + teamId, e);
            return null;
        }
    }

    @Override
    public Team findTeamByCode(String teamCode) {
        try {
            return em.createNamedQuery("Team.findByCode", Team.class)
                    .setParameter("teamCode", teamCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding team by code: " + teamCode, e);
            return null;
        }
    }

    @Override
    public List<Team> getAllTeams() {
        try {
            return em.createNamedQuery("Team.findAll", Team.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all teams", e);
            return List.of();
        }
    }

    @Override
    public List<Team> getActiveTeams() {
        try {
            return em.createNamedQuery("Team.findAll", Team.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active teams", e);
            return List.of();
        }
    }

    @Override
    public List<Team> getTeamsByCountry(String country) {
        try {
            return em.createNamedQuery("Team.findByCountry", Team.class)
                    .setParameter("country", country)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting teams by country: " + country, e);
            return List.of();
        }
    }

    @Override
    public void updateTeam(Team team) {
        try {
            em.merge(team);
            logger.info("Team updated: " + team.getTeamName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating team: " + team.getTeamName(), e);
            throw new RuntimeException("Failed to update team", e);
        }
    }

    @Override
    public void updateTeamStats(Long teamId) {
        try {
            Team team = findTeamById(teamId);
            if (team != null) {
                // Get total matches and wins for the team
                TypedQuery<Long> totalQuery = em.createQuery(
                        "SELECT COUNT(m) FROM Match m WHERE (m.team1.id = :teamId OR m.team2.id = :teamId) AND m.status = 'COMPLETED'",
                        Long.class);
                totalQuery.setParameter("teamId", teamId);
                Long totalMatches = totalQuery.getSingleResult();

                TypedQuery<Long> winsQuery = em.createQuery(
                        "SELECT COUNT(m) FROM Match m WHERE m.winnerTeam.id = :teamId AND m.status = 'COMPLETED'",
                        Long.class);
                winsQuery.setParameter("teamId", teamId);
                Long wins = winsQuery.getSingleResult();

                team.updateWinRate(wins.intValue(), totalMatches.intValue());
                updateTeam(team);

                logger.info("Team stats updated for: " + team.getTeamName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating team stats: " + teamId, e);
        }
    }

    // Tournament Management
    @Override
    public Tournament createTournament(String tournamentName, Tournament.TournamentType type,
                                       java.time.LocalDate startDate, java.time.LocalDate endDate,
                                       BigDecimal prizePool, String description) {
        try {
            Tournament tournament = new Tournament(tournamentName, type, startDate, endDate);
            tournament.setPrizePool(prizePool);
            tournament.setDescription(description);

            em.persist(tournament);
            em.flush();

            logger.info("Tournament created: " + tournamentName);
            return tournament;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating tournament: " + tournamentName, e);
            throw new RuntimeException("Failed to create tournament", e);
        }
    }

    @Override
    public Tournament findTournamentById(Long tournamentId) {
        try {
            return em.find(Tournament.class, tournamentId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding tournament by ID: " + tournamentId, e);
            return null;
        }
    }

    @Override
    public List<Tournament> getAllTournaments() {
        try {
            return em.createNamedQuery("Tournament.findAll", Tournament.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all tournaments", e);
            return List.of();
        }
    }

    @Override
    public List<Tournament> getActiveTournaments() {
        try {
            return em.createNamedQuery("Tournament.findActive", Tournament.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active tournaments", e);
            return List.of();
        }
    }

    @Override
    public List<Tournament> getUpcomingTournaments() {
        try {
            return em.createNamedQuery("Tournament.findUpcoming", Tournament.class)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming tournaments", e);
            return List.of();
        }
    }

    @Override
    public List<Tournament> getCompletedTournaments() {
        try {
            return em.createNamedQuery("Tournament.findByStatus", Tournament.class)
                    .setParameter("status", Tournament.TournamentStatus.COMPLETED)
                    .getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting completed tournaments", e);
            return List.of();
        }
    }

    @Override
    public void updateTournament(Tournament tournament) {
        try {
            em.merge(tournament);
            logger.info("Tournament updated: " + tournament.getTournamentName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating tournament: " + tournament.getTournamentName(), e);
            throw new RuntimeException("Failed to update tournament", e);
        }
    }

    @Override
    public void startTournament(Long tournamentId) {
        try {
            Tournament tournament = findTournamentById(tournamentId);
            if (tournament != null) {
                tournament.startTournament();
                updateTournament(tournament);
                logger.info("Tournament started: " + tournament.getTournamentName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting tournament: " + tournamentId, e);
            throw new RuntimeException("Failed to start tournament", e);
        }
    }

    @Override
    public void completeTournament(Long tournamentId) {
        try {
            Tournament tournament = findTournamentById(tournamentId);
            if (tournament != null) {
                tournament.completeTournament();
                updateTournament(tournament);
                logger.info("Tournament completed: " + tournament.getTournamentName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error completing tournament: " + tournamentId, e);
            throw new RuntimeException("Failed to complete tournament", e);
        }
    }

    // Match Statistics
    @Override
    public BigDecimal getTotalBetPool(Long matchId) {
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(b.betAmount), 0) FROM Bet b WHERE b.match.id = :matchId",
                    BigDecimal.class);
            query.setParameter("matchId", matchId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total bet pool for match: " + matchId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal getTeamBetPool(Long matchId, Long teamId) {
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(b.betAmount), 0) FROM Bet b WHERE b.match.id = :matchId AND b.selectedTeam.id = :teamId",
                    BigDecimal.class);
            query.setParameter("matchId", matchId);
            query.setParameter("teamId", teamId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting team bet pool", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public int getTotalBetsCount(Long matchId) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.match.id = :matchId",
                    Long.class);
            query.setParameter("matchId", matchId);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total bets count for match: " + matchId, e);
            return 0;
        }
    }

    @Override
    public int getTeamBetsCount(Long matchId, Long teamId) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.match.id = :matchId AND b.selectedTeam.id = :teamId",
                    Long.class);
            query.setParameter("matchId", matchId);
            query.setParameter("teamId", teamId);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting team bets count", e);
            return 0;
        }
    }

    // Validation
    @Override
    public boolean isMatchBettable(Long matchId) {
        Match match = findMatchById(matchId);
        return match != null && match.canPlaceBet();
    }

    @Override
    public boolean canUpdateMatch(Long matchId) {
        Match match = findMatchById(matchId);
        return match != null && match.getStatus() != Match.MatchStatus.COMPLETED;
    }

    @Override
    public boolean isValidMatchup(Long team1Id, Long team2Id) {
        return !team1Id.equals(team2Id);
    }

    // Search and Filter
    @Override
    public List<Match> searchMatches(String keyword) {
        try {
            TypedQuery<Match> query = em.createQuery(
                    "SELECT m FROM Match m WHERE " +
                            "LOWER(m.team1.teamName) LIKE LOWER(:keyword) OR " +
                            "LOWER(m.team2.teamName) LIKE LOWER(:keyword) OR " +
                            "LOWER(m.tournament.tournamentName) LIKE LOWER(:keyword) " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error searching matches with keyword: " + keyword, e);
            return List.of();
        }
    }

    @Override
    public List<Match> filterMatchesByStatus(Match.MatchStatus status) {
        try {
            TypedQuery<Match> query = em.createQuery(
                    "SELECT m FROM Match m WHERE m.status = :status ORDER BY m.matchDate DESC",
                    Match.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering matches by status: " + status, e);
            return List.of();
        }
    }

    @Override
    public List<Match> filterMatchesByType(Match.MatchType type) {
        try {
            TypedQuery<Match> query = em.createQuery(
                    "SELECT m FROM Match m WHERE m.matchType = :type ORDER BY m.matchDate DESC",
                    Match.class);
            query.setParameter("type", type);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering matches by type: " + type, e);
            return List.of();
        }
    }
}