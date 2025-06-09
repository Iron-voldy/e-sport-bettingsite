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
import java.util.ArrayList;
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
    public List<Match> getUpcomingMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.status = :status AND m.matchDate > :currentTime " +
                            "ORDER BY m.matchDate",
                    Match.class);

            query.setParameter("status", Match.MatchStatus.SCHEDULED);
            query.setParameter("currentTime", LocalDateTime.now());

            List<Match> matches = query.getResultList();
            logger.info("Retrieved " + matches.size() + " upcoming matches");
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming matches", e);
            return new ArrayList<>();
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
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.status = :status " +
                            "ORDER BY m.matchDate",
                    Match.class);

            query.setParameter("status", Match.MatchStatus.LIVE);

            List<Match> matches = query.getResultList();
            logger.info("Retrieved " + matches.size() + " live matches");
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting live matches", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> getAllMatches() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);

            List<Match> matches = query.getResultList();
            logger.info("Retrieved " + matches.size() + " total matches");
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all matches", e);
            return new ArrayList<>();
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
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "LEFT JOIN FETCH m.winnerTeam " +
                            "WHERE m.status = :status " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);

            query.setParameter("status", Match.MatchStatus.COMPLETED);

            List<Match> matches = query.getResultList();
            logger.info("Retrieved " + matches.size() + " completed matches");
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting completed matches", e);
            return new ArrayList<>();
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
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.tournament.id = :tournamentId " +
                            "ORDER BY m.matchDate",
                    Match.class);

            query.setParameter("tournamentId", tournamentId);

            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by tournament: " + tournamentId, e);
            return new ArrayList<>();
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
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.bettingEnabled = true AND m.status = :status AND m.matchDate > :currentTime " +
                            "ORDER BY m.matchDate",
                    Match.class);

            query.setParameter("status", Match.MatchStatus.SCHEDULED);
            query.setParameter("currentTime", LocalDateTime.now());

            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting bettable matches", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Match findMatchById(Long matchId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "LEFT JOIN FETCH m.winnerTeam " +
                            "WHERE m.id = :matchId",
                    Match.class);

            query.setParameter("matchId", matchId);
            return query.getSingleResult();

        } catch (NoResultException e) {
            logger.warning("Match not found with ID: " + matchId);
            return null;
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
    public List<Match> getMatchesByTeam(Long teamId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.team1.id = :teamId OR m.team2.id = :teamId " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);

            query.setParameter("teamId", teamId);

            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by team: " + teamId, e);
            return new ArrayList<>();
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
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.matchDate BETWEEN :startDate AND :endDate " +
                            "ORDER BY m.matchDate",
                    Match.class);

            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);

            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting matches by date range", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
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
            List<Team> teams = query.getResultList();
            return teams != null ? teams : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all teams", e);
            return new ArrayList<>();
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
            List<Team> teams = query.getResultList();
            return teams != null ? teams : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active teams", e);
            return new ArrayList<>();
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
            List<Team> teams = query.getResultList();
            return teams != null ? teams : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting teams by country: " + country, e);
            return new ArrayList<>();
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
            List<Tournament> tournaments = query.getResultList();
            return tournaments != null ? tournaments : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all tournaments", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    // Other required interface methods with implementations
    @Override
    public void startMatch(Long matchId) {
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
            if (match != null && match.getStatus() == Match.MatchStatus.SCHEDULED) {
                match.startMatch();
                entityManager.merge(match);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Match started: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error starting match: " + matchId, e);
            throw new RuntimeException("Failed to start match", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void completeMatch(Long matchId, Long winnerTeamId, int team1Score, int team2Score) {
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
            Team winnerTeam = findTeamById(winnerTeamId);

            if (match != null && winnerTeam != null) {
                match.completeMatch(winnerTeam, team1Score, team2Score);
                entityManager.merge(match);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Match completed: " + match.getMatchTitle() + ", Winner: " + winnerTeam.getTeamName());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error completing match: " + matchId, e);
            throw new RuntimeException("Failed to complete match", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void cancelMatch(Long matchId) {
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
                match.setStatus(Match.MatchStatus.CANCELLED);
                match.setBettingEnabled(false);
                entityManager.merge(match);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Match cancelled: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error cancelling match: " + matchId, e);
            throw new RuntimeException("Failed to cancel match", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void enableBetting(Long matchId) {
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
            if (match != null && match.getStatus() == Match.MatchStatus.SCHEDULED) {
                match.setBettingEnabled(true);
                entityManager.merge(match);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Betting enabled for match: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error enabling betting for match: " + matchId, e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void disableBetting(Long matchId) {
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
                match.setBettingEnabled(false);
                entityManager.merge(match);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Betting disabled for match: " + match.getMatchTitle());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error disabling betting for match: " + matchId, e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateOdds(Long matchId, BigDecimal team1Odds, BigDecimal team2Odds) {
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
                match.updateOdds(team1Odds, team2Odds);
                entityManager.merge(match);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Odds updated for match: " + match.getMatchTitle() +
                        " (" + team1Odds + " / " + team2Odds + ")");
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error updating odds for match: " + matchId, e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public BigDecimal calculateDynamicOdds(Long matchId, Long teamId) {
        try {
            Match match = findMatchById(matchId);
            if (match == null) {
                return new BigDecimal("2.00");
            }

            if (teamId.equals(match.getTeam1().getId())) {
                return match.getTeam1Odds();
            } else if (teamId.equals(match.getTeam2().getId())) {
                return match.getTeam2Odds();
            }

            return new BigDecimal("2.00");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error calculating dynamic odds", e);
            return new BigDecimal("2.00");
        }
    }

    @Override
    public void recalculateOdds(Long matchId) {
        try {
            // Basic odds recalculation logic
            Match match = findMatchById(matchId);
            if (match != null && match.canPlaceBet()) {
                // Simple odds adjustment based on betting volume
                BigDecimal team1NewOdds = match.getTeam1Odds().multiply(new BigDecimal("0.98"));
                BigDecimal team2NewOdds = match.getTeam2Odds().multiply(new BigDecimal("1.02"));

                updateOdds(matchId, team1NewOdds, team2NewOdds);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error recalculating odds for match: " + matchId, e);
        }
    }

    @Override
    public void updateTeam(Team team) {
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

            entityManager.merge(team);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            }

            logger.info("Team updated: " + team.getTeamName());
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error updating team: " + team.getId(), e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateTeamStats(Long teamId) {
        try {
            Team team = findTeamById(teamId);
            if (team != null) {
                // Calculate win rate and update team stats
                List<Match> teamMatches = getMatchesByTeam(teamId);
                int wins = 0;
                int totalMatches = 0;

                for (Match match : teamMatches) {
                    if (match.isCompleted()) {
                        totalMatches++;
                        if (match.getWinnerTeam() != null && match.getWinnerTeam().getId().equals(teamId)) {
                            wins++;
                        }
                    }
                }

                team.updateWinRate(wins, totalMatches);
                updateTeam(team);

                logger.info("Team stats updated: " + team.getTeamName() +
                        " (Win Rate: " + team.getWinRate() + "%)");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error updating team stats for team: " + teamId, e);
        }
    }

    @Override
    public List<Tournament> getActiveTournaments() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Tournament> query = entityManager.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status = :status ORDER BY t.startDate", Tournament.class);
            query.setParameter("status", Tournament.TournamentStatus.ONGOING);
            List<Tournament> tournaments = query.getResultList();
            return tournaments != null ? tournaments : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active tournaments", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Tournament> getUpcomingTournaments() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Tournament> query = entityManager.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status = :status ORDER BY t.startDate", Tournament.class);
            query.setParameter("status", Tournament.TournamentStatus.UPCOMING);
            List<Tournament> tournaments = query.getResultList();
            return tournaments != null ? tournaments : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming tournaments", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Tournament> getCompletedTournaments() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Tournament> query = entityManager.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status = :status ORDER BY t.endDate DESC", Tournament.class);
            query.setParameter("status", Tournament.TournamentStatus.COMPLETED);
            List<Tournament> tournaments = query.getResultList();
            return tournaments != null ? tournaments : new ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting completed tournaments", e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateTournament(Tournament tournament) {
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

            entityManager.merge(tournament);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            }

            logger.info("Tournament updated: " + tournament.getTournamentName());
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error updating tournament: " + tournament.getId(), e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void startTournament(Long tournamentId) {
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

            Tournament tournament = entityManager.find(Tournament.class, tournamentId);
            if (tournament != null) {
                tournament.startTournament();
                entityManager.merge(tournament);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Tournament started: " + tournament.getTournamentName());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error starting tournament: " + tournamentId, e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void completeTournament(Long tournamentId) {
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

            Tournament tournament = entityManager.find(Tournament.class, tournamentId);
            if (tournament != null) {
                tournament.completeTournament();
                entityManager.merge(tournament);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Tournament completed: " + tournament.getTournamentName());
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error completing tournament: " + tournamentId, e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public BigDecimal getTotalBetPool(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            return match != null ? match.getTotalPool() : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting total bet pool for match: " + matchId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal getTeamBetPool(Long matchId, Long teamId) {
        // This would require integration with BettingService
        return BigDecimal.ZERO;
    }

    @Override
    public int getTotalBetsCount(Long matchId) {
        // This would require integration with BettingService
        return 0;
    }

    @Override
    public int getTeamBetsCount(Long matchId, Long teamId) {
        // This would require integration with BettingService
        return 0;
    }

    @Override
    public boolean isMatchBettable(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            return match != null && match.canPlaceBet();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking if match is bettable: " + matchId, e);
            return false;
        }
    }

    @Override
    public boolean canUpdateMatch(Long matchId) {
        try {
            Match match = findMatchById(matchId);
            return match != null && match.getStatus() != Match.MatchStatus.COMPLETED;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking if match can be updated: " + matchId, e);
            return false;
        }
    }

    @Override
    public boolean isValidMatchup(Long team1Id, Long team2Id) {
        return team1Id != null && team2Id != null && !team1Id.equals(team2Id);
    }

    @Override
    public List<Match> searchMatches(String keyword) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE LOWER(m.team1.teamName) LIKE LOWER(:keyword) " +
                            "OR LOWER(m.team2.teamName) LIKE LOWER(:keyword) " +
                            "OR LOWER(m.tournament.tournamentName) LIKE LOWER(:keyword) " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);

            query.setParameter("keyword", "%" + keyword + "%");
            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error searching matches with keyword: " + keyword, e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> filterMatchesByStatus(Match.MatchStatus status) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.status = :status " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);

            query.setParameter("status", status);
            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering matches by status: " + status, e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Match> filterMatchesByType(Match.MatchType type) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Match> query = entityManager.createQuery(
                    "SELECT m FROM Match m " +
                            "LEFT JOIN FETCH m.tournament " +
                            "LEFT JOIN FETCH m.team1 " +
                            "LEFT JOIN FETCH m.team2 " +
                            "WHERE m.matchType = :type " +
                            "ORDER BY m.matchDate DESC",
                    Match.class);

            query.setParameter("type", type);
            List<Match> matches = query.getResultList();
            return matches != null ? matches : new ArrayList<>();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering matches by type: " + type, e);
            return new ArrayList<>();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}