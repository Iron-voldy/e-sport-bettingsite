package lk.esports.betting.ejb;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Team;
import lk.esports.betting.entity.Tournament;
import lk.esports.betting.utils.DatabaseUtil;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.PostConstruct;

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

    @PostConstruct
    public void init() {
        logger.info("MatchServiceBean initialized");
    }

    // Helper method to get EntityManager with fallback
    private EntityManager getEntityManager() {
        if (em == null) {
            logger.warning("EntityManager is null, creating new one using DatabaseUtil");
            return DatabaseUtil.createEntityManager();
        }
        return em;
    }

    @Override
    public Match createMatch(Long tournamentId, Long team1Id, Long team2Id, LocalDateTime matchDate,
                             Match.MatchType matchType) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
            }

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

            entityManager.persist(match);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            } else {
                entityManager.flush();
            }

            logger.info("Match created: " + match.getMatchTitle());
            return match;

        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error creating match", e);
            throw new RuntimeException("Failed to create match", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Match findMatchById(Long matchId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            return entityManager.find(Match.class, matchId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding match by ID: " + matchId, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateMatch(Match match) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
            }

            match.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(match);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            }

            logger.info("Match updated: " + match.getMatchTitle());
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error updating match: " + match.getId(), e);
            throw new RuntimeException("Failed to update match", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void deleteMatch(Long matchId) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
            }

            Match match = entityManager.find(Match.class, matchId);
            if (match != null) {
                entityManager.remove(match);
                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }
                logger.info("Match deleted: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error deleting match: " + matchId, e);
            throw new RuntimeException("Failed to delete match", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getAllMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery("SELECT m FROM Match m ORDER BY m.matchDate DESC", Match.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all matches", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getUpcomingMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.status = 'SCHEDULED' AND m.matchDate > CURRENT_TIMESTAMP ORDER BY m.matchDate",
                    Match.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming matches", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getLiveMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.status = 'LIVE' ORDER BY m.matchDate",
                    Match.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting live matches", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getCompletedMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.status = 'COMPLETED' ORDER BY m.matchDate DESC",
                    Match.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting completed matches", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getMatchesByTournament(Long tournamentId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.tournament.id = :tournamentId ORDER BY m.matchDate",
                    Match.class);
            query.setParameter("tournamentId", tournamentId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by tournament: " + tournamentId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getBettableMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.bettingEnabled = true AND m.status = 'SCHEDULED' AND m.matchDate > CURRENT_TIMESTAMP ORDER BY m.matchDate",
                    Match.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting bettable matches", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getMatchesByTeam(Long teamId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.team1.id = :teamId OR m.team2.id = :teamId ORDER BY m.matchDate DESC",
                    Match.class);
            query.setParameter("teamId", teamId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by team: " + teamId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getMatchesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate ORDER BY m.matchDate",
                    Match.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by date range", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    // Team Management Methods
    @Override
    public Team createTeam(String teamName, String teamCode, String country, String logoUrl, String description) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
            }

            // Check if team code already exists
            Team existingTeam = findTeamByCode(teamCode);
            if (existingTeam != null) {
                throw new IllegalArgumentException("Team code already exists: " + teamCode);
            }

            Team team = new Team(teamName, teamCode, country);
            team.setLogoUrl(logoUrl);
            team.setDescription(description);

            entityManager.persist(team);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            } else {
                entityManager.flush();
            }

            logger.info("Team created: " + teamName + " (" + teamCode + ")");
            return team;

        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error creating team: " + teamName, e);
            throw new RuntimeException("Failed to create team", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Team findTeamById(Long teamId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            return entityManager.find(Team.class, teamId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding team by ID: " + teamId, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Team findTeamByCode(String teamCode) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Team> query = entityManager.createQuery(
                    "SELECT t FROM Team t WHERE t.teamCode = :teamCode", Team.class);
            query.setParameter("teamCode", teamCode);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding team by code: " + teamCode, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Team> getAllTeams() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Team> query = entityManager.createQuery(
                    "SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.teamName", Team.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all teams", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Team> getActiveTeams() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Team> query = entityManager.createQuery(
                    "SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.teamName", Team.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active teams", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Team> getTeamsByCountry(String country) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Team> query = entityManager.createQuery(
                    "SELECT t FROM Team t WHERE t.country = :country AND t.isActive = true ORDER BY t.teamName", Team.class);
            query.setParameter("country", country);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting teams by country: " + country, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    // Tournament Management Methods
    @Override
    public Tournament createTournament(String tournamentName, Tournament.TournamentType type,
                                       java.time.LocalDate startDate, java.time.LocalDate endDate,
                                       BigDecimal prizePool, String description) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
            }

            Tournament tournament = new Tournament(tournamentName, type, startDate, endDate);
            tournament.setPrizePool(prizePool);
            tournament.setDescription(description);

            entityManager.persist(tournament);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            } else {
                entityManager.flush();
            }

            logger.info("Tournament created: " + tournamentName);
            return tournament;

        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error creating tournament: " + tournamentName, e);
            throw new RuntimeException("Failed to create tournament", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Tournament findTournamentById(Long tournamentId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            return entityManager.find(Tournament.class, tournamentId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding tournament by ID: " + tournamentId, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Tournament> getAllTournaments() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Tournament> query = entityManager.createQuery(
                    "SELECT t FROM Tournament t ORDER BY t.startDate DESC", Tournament.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all tournaments", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    // Other required interface methods (simplified implementations)
    @Override
    public void startMatch(Long matchId) {
        // Implementation
    }

    @Override
    public void completeMatch(Long matchId, Long winnerTeamId, int team1Score, int team2Score) {
        // Implementation
    }

    @Override
    public void cancelMatch(Long matchId) {
        // Implementation
    }

    @Override
    public void enableBetting(Long matchId) {
        // Implementation
    }

    @Override
    public void disableBetting(Long matchId) {
        // Implementation
    }

    @Override
    public void updateOdds(Long matchId, BigDecimal team1Odds, BigDecimal team2Odds) {
        // Implementation
    }

    @Override
    public BigDecimal calculateDynamicOdds(Long matchId, Long teamId) {
        return new BigDecimal("2.00");
    }

    @Override
    public void recalculateOdds(Long matchId) {
        // Implementation
    }

    @Override
    public void updateTeam(Team team) {
        // Implementation
    }

    @Override
    public void updateTeamStats(Long teamId) {
        // Implementation
    }

    @Override
    public List<Tournament> getActiveTournaments() {
        return getAllTournaments();
    }

    @Override
    public List<Tournament> getUpcomingTournaments() {
        return getAllTournaments();
    }

    @Override
    public List<Tournament> getCompletedTournaments() {
        return getAllTournaments();
    }

    @Override
    public void updateTournament(Tournament tournament) {
        // Implementation
    }

    @Override
    public void startTournament(Long tournamentId) {
        // Implementation
    }

    @Override
    public void completeTournament(Long tournamentId) {
        // Implementation
    }

    @Override
    public BigDecimal getTotalBetPool(Long matchId) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTeamBetPool(Long matchId, Long teamId) {
        return BigDecimal.ZERO;
    }

    @Override
    public int getTotalBetsCount(Long matchId) {
        return 0;
    }

    @Override
    public int getTeamBetsCount(Long matchId, Long teamId) {
        return 0;
    }

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

    @Override
    public List<Match> searchMatches(String keyword) {
        return getAllMatches();
    }

    @Override
    public List<Match> filterMatchesByStatus(Match.MatchStatus status) {
        return getAllMatches();
    }

    @Override
    public List<Match> filterMatchesByType(Match.MatchType type) {
        return getAllMatches();
    }
}