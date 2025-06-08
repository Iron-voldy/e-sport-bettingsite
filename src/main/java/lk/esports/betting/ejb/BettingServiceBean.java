package lk.esports.betting.ejb;

import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.entity.Bet;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.User;
import lk.esports.betting.entity.Team;
import lk.esports.betting.entity.Transaction;
import lk.esports.betting.utils.DatabaseUtil;
import lk.esports.betting.utils.EJBServiceLocator;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class BettingServiceBean implements BettingService {

    private static final Logger logger = Logger.getLogger(BettingServiceBean.class.getName());
    private static final BigDecimal MIN_BET_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_BET_AMOUNT = new BigDecimal("10000.00");

    @PersistenceContext(unitName = "esportsPU")
    private EntityManager em;

    @EJB
    private UserService userService;

    @EJB
    private MatchService matchService;

    @PostConstruct
    public void init() {
        logger.info("BettingServiceBean initialized");
    }

    // Helper method to get EntityManager with fallback
    private EntityManager getEntityManager() {
        if (em == null) {
            logger.warning("EntityManager is null, creating new one using DatabaseUtil");
            return DatabaseUtil.createEntityManager();
        }
        return em;
    }

    // Helper method to get services with fallback
    private UserService getUserService() {
        if (userService == null) {
            return EJBServiceLocator.getUserService();
        }
        return userService;
    }

    private MatchService getMatchService() {
        if (matchService == null) {
            return EJBServiceLocator.getMatchService();
        }
        return matchService;
    }

    @Override
    public Bet placeBet(Long userId, Long matchId, Long selectedTeamId, BigDecimal betAmount) {
        EntityManager entityManager = null;
        boolean useLocalTransaction = false;

        try {
            if (!validateBet(userId, matchId, selectedTeamId, betAmount)) {
                throw new IllegalArgumentException("Invalid bet parameters");
            }

            UserService userSvc = getUserService();
            MatchService matchSvc = getMatchService();

            User user = userSvc.findUserById(userId);
            Match match = matchSvc.findMatchById(matchId);
            Team selectedTeam = matchSvc.findTeamById(selectedTeamId);

            if (user == null || match == null || selectedTeam == null) {
                throw new IllegalArgumentException("Invalid entities");
            }

            // Check if user has already bet on this match
            if (hasUserBetOnMatch(userId, matchId)) {
                throw new IllegalArgumentException("User has already placed a bet on this match");
            }

            // Get current odds for the selected team
            BigDecimal currentOdds = match.getOddsForTeam(selectedTeam);

            // Deduct funds from user wallet
            if (!userSvc.deductFunds(userId, betAmount, "Bet placed on " + match.getMatchTitle())) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            entityManager = getEntityManager();

            if (em == null) {
                useLocalTransaction = true;
                if (!entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().begin();
                }
            }

            // Create bet
            Bet bet = new Bet(user, match, selectedTeam, betAmount, currentOdds);
            entityManager.persist(bet);

            if (useLocalTransaction) {
                entityManager.getTransaction().commit();
            } else {
                entityManager.flush();
            }

            // Create transaction record
            userSvc.createTransaction(userId, Transaction.TransactionType.BET_PLACED,
                    betAmount, "Bet placed on " + match.getMatchTitle(), bet.getId());

            logger.info("Bet placed: User " + user.getUsername() + " bet $" + betAmount +
                    " on " + selectedTeam.getTeamName() + " for match " + match.getMatchTitle());

            return bet;

        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error placing bet", e);
            throw new RuntimeException("Failed to place bet: " + e.getMessage(), e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean validateBet(Long userId, Long matchId, Long selectedTeamId, BigDecimal betAmount) {
        try {
            UserService userSvc = getUserService();
            MatchService matchSvc = getMatchService();

            // Check user
            if (!userSvc.isAccountActive(userId)) {
                return false;
            }

            // Check bet amount
            if (!isValidBetAmount(betAmount)) {
                return false;
            }

            // Check if user can afford the bet
            if (!canPlaceBet(userId, matchId, betAmount)) {
                return false;
            }

            // Check match
            if (!isMatchBettable(matchId)) {
                return false;
            }

            // Check if selected team is participating in the match
            Match match = matchSvc.findMatchById(matchId);
            if (match == null) {
                return false;
            }

            return selectedTeamId.equals(match.getTeam1().getId()) ||
                    selectedTeamId.equals(match.getTeam2().getId());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error validating bet", e);
            return false;
        }
    }

    @Override
    public BigDecimal calculatePotentialWinnings(Long matchId, Long selectedTeamId, BigDecimal betAmount) {
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            Team selectedTeam = matchSvc.findTeamById(selectedTeamId);

            if (match != null && selectedTeam != null && betAmount != null) {
                BigDecimal odds = match.getOddsForTeam(selectedTeam);
                return betAmount.multiply(odds).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating potential winnings", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Bet findBetById(Long betId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            return entityManager.find(Bet.class, betId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding bet by ID: " + betId, e);
            return null;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getUserBets(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.user.id = :userId ORDER BY b.betPlacedAt DESC", Bet.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user bets: " + userId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getMatchBets(Long matchId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.match.id = :matchId ORDER BY b.betPlacedAt DESC", Bet.class);
            query.setParameter("matchId", matchId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting match bets: " + matchId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getBetsByStatus(Bet.BetStatus status) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.status = :status ORDER BY b.betPlacedAt DESC", Bet.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting bets by status: " + status, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getUserBetsByMatch(Long userId, Long matchId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.user.id = :userId AND b.match.id = :matchId", Bet.class);
            query.setParameter("userId", userId);
            query.setParameter("matchId", matchId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user bets by match", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getPendingBets() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.status = 'PENDING' ORDER BY b.betPlacedAt", Bet.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting pending bets", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getUserPendingBets(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.user.id = :userId AND b.status = 'PENDING' ORDER BY b.betPlacedAt DESC",
                    Bet.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user pending bets: " + userId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void processBetResults(Long matchId) {
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            if (match == null || !match.isCompleted()) {
                logger.warning("Cannot process bets for incomplete match: " + matchId);
                return;
            }

            List<Bet> matchBets = getMatchBets(matchId);
            Team winnerTeam = match.getWinnerTeam();

            for (Bet bet : matchBets) {
                if (bet.isPending()) {
                    if (bet.getSelectedTeam().equals(winnerTeam)) {
                        markBetAsWon(bet.getId());
                    } else {
                        markBetAsLost(bet.getId());
                    }
                }
            }

            logger.info("Processed " + matchBets.size() + " bets for match: " + match.getMatchTitle());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing bet results for match: " + matchId, e);
            throw new RuntimeException("Failed to process bet results", e);
        }
    }

    @Override
    public void markBetAsWon(Long betId) {
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

            Bet bet = entityManager.find(Bet.class, betId);
            if (bet != null && bet.isPending()) {
                bet.markAsWon();
                entityManager.merge(bet);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                // Add winnings to user wallet
                BigDecimal winnings = bet.getPotentialWinnings();
                UserService userSvc = getUserService();
                userSvc.addFunds(bet.getUser().getId(), winnings,
                        "Winnings from bet on " + bet.getMatch().getMatchTitle());

                // Create transaction record
                userSvc.createTransaction(bet.getUser().getId(), Transaction.TransactionType.WINNINGS,
                        winnings, "Winnings from bet on " + bet.getMatch().getMatchTitle(), betId);

                logger.info("Bet marked as won: " + betId + ", Winnings: $" + winnings);
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error marking bet as won: " + betId, e);
            throw new RuntimeException("Failed to mark bet as won", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void markBetAsLost(Long betId) {
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

            Bet bet = entityManager.find(Bet.class, betId);
            if (bet != null && bet.isPending()) {
                bet.markAsLost();
                entityManager.merge(bet);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                logger.info("Bet marked as lost: " + betId);
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error marking bet as lost: " + betId, e);
            throw new RuntimeException("Failed to mark bet as lost", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void cancelBet(Long betId) {
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

            Bet bet = entityManager.find(Bet.class, betId);
            if (bet != null && bet.canBeCancelled()) {
                bet.markAsCancelled();
                entityManager.merge(bet);

                if (useLocalTransaction) {
                    entityManager.getTransaction().commit();
                }

                // Refund bet amount to user
                refundBet(betId);

                logger.info("Bet cancelled: " + betId);
            }
        } catch (Exception e) {
            if (useLocalTransaction && entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error cancelling bet: " + betId, e);
            throw new RuntimeException("Failed to cancel bet", e);
        } finally {
            if (useLocalTransaction && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void refundBet(Long betId) {
        try {
            Bet bet = findBetById(betId);
            if (bet != null) {
                UserService userSvc = getUserService();
                // Refund bet amount to user wallet
                userSvc.refundFunds(bet.getUser().getId(), bet.getBetAmount(),
                        "Refund for cancelled bet on " + bet.getMatch().getMatchTitle());

                logger.info("Bet refunded: " + betId + ", Amount: $" + bet.getBetAmount());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error refunding bet: " + betId, e);
            throw new RuntimeException("Failed to refund bet", e);
        }
    }

    @Override
    public boolean canPlaceBet(Long userId, Long matchId, BigDecimal amount) {
        UserService userSvc = getUserService();
        return userSvc.canPlaceBet(userId, amount) &&
                isMatchBettable(matchId) &&
                isValidBetAmount(amount) &&
                !hasUserBetOnMatch(userId, matchId);
    }

    @Override
    public boolean isValidBetAmount(BigDecimal amount) {
        return amount != null &&
                amount.compareTo(MIN_BET_AMOUNT) >= 0 &&
                amount.compareTo(MAX_BET_AMOUNT) <= 0;
    }

    @Override
    public boolean hasUserBetOnMatch(Long userId, Long matchId) {
        try {
            List<Bet> userBets = getUserBetsByMatch(userId, matchId);
            return !userBets.isEmpty();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking if user has bet on match", e);
            return false;
        }
    }

    @Override
    public boolean isMatchBettable(Long matchId) {
        MatchService matchSvc = getMatchService();
        return matchSvc.isMatchBettable(matchId);
    }

    @Override
    public BigDecimal getTotalBetAmount(Long matchId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<BigDecimal> query = entityManager.createQuery(
                    "SELECT COALESCE(SUM(b.betAmount), 0) FROM Bet b WHERE b.match.id = :matchId", BigDecimal.class);
            query.setParameter("matchId", matchId);
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total bet amount for match: " + matchId, e);
            return BigDecimal.ZERO;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public BigDecimal getTeamBetAmount(Long matchId, Long teamId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<BigDecimal> query = entityManager.createQuery(
                    "SELECT COALESCE(SUM(b.betAmount), 0) FROM Bet b WHERE b.match.id = :matchId AND b.selectedTeam.id = :teamId",
                    BigDecimal.class);
            query.setParameter("matchId", matchId);
            query.setParameter("teamId", teamId);
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting team bet amount", e);
            return BigDecimal.ZERO;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public int getTotalBetsCount(Long matchId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.match.id = :matchId", Long.class);
            query.setParameter("matchId", matchId);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total bets count for match: " + matchId, e);
            return 0;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public int getTeamBetsCount(Long matchId, Long teamId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.match.id = :matchId AND b.selectedTeam.id = :teamId", Long.class);
            query.setParameter("matchId", matchId);
            query.setParameter("teamId", teamId);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting team bets count", e);
            return 0;
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public Map<Long, BigDecimal> getTeamBetDistribution(Long matchId) {
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            if (match == null) {
                return new HashMap<>();
            }

            Map<Long, BigDecimal> distribution = new HashMap<>();
            distribution.put(match.getTeam1().getId(), getTeamBetAmount(matchId, match.getTeam1().getId()));
            distribution.put(match.getTeam2().getId(), getTeamBetAmount(matchId, match.getTeam2().getId()));

            return distribution;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting team bet distribution for match: " + matchId, e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Bet> getUserWinningBets(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.user.id = :userId AND b.status = 'WON' ORDER BY b.resultProcessedAt DESC",
                    Bet.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user winning bets: " + userId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getUserLosingBets(Long userId) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.user.id = :userId AND b.status = 'LOST' ORDER BY b.resultProcessedAt DESC",
                    Bet.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user losing bets: " + userId, e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public BigDecimal getUserTotalBetAmount(Long userId) {
        UserService userSvc = getUserService();
        return userSvc.getTotalBetAmount(userId);
    }

    @Override
    public BigDecimal getUserTotalWinnings(Long userId) {
        UserService userSvc = getUserService();
        return userSvc.getTotalWinnings(userId);
    }

    @Override
    public double getUserWinRate(Long userId) {
        UserService userSvc = getUserService();
        return userSvc.getWinRate(userId);
    }

    // Additional required methods with basic implementations
    @Override
    public BigDecimal calculateCurrentOdds(Long matchId, Long teamId) {
        try {
            BigDecimal totalPool = getTotalBetAmount(matchId);
            BigDecimal teamPool = getTeamBetAmount(matchId, teamId);

            if (totalPool.compareTo(BigDecimal.ZERO) == 0 || teamPool.compareTo(BigDecimal.ZERO) == 0) {
                return new BigDecimal("2.00"); // Default odds
            }

            // Calculate odds with house edge
            BigDecimal margin = new BigDecimal("0.95"); // 5% house edge
            BigDecimal rawOdds = totalPool.divide(teamPool, 4, java.math.RoundingMode.HALF_UP);
            BigDecimal adjustedOdds = rawOdds.multiply(margin);

            // Ensure minimum odds of 1.01
            BigDecimal minOdds = new BigDecimal("1.01");
            return adjustedOdds.max(minOdds).setScale(2, java.math.RoundingMode.HALF_UP);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating current odds", e);
            return new BigDecimal("2.00");
        }
    }

    @Override
    public void updateMatchOdds(Long matchId) {
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            if (match != null) {
                BigDecimal team1Odds = calculateCurrentOdds(matchId, match.getTeam1().getId());
                BigDecimal team2Odds = calculateCurrentOdds(matchId, match.getTeam2().getId());

                matchSvc.updateOdds(matchId, team1Odds, team2Odds);
                logger.info("Odds updated for match " + matchId + ": " + team1Odds + " / " + team2Odds);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating match odds: " + matchId, e);
        }
    }

    @Override
    public Map<Long, BigDecimal> getCurrentOddsForMatch(Long matchId) {
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            if (match == null) {
                return new HashMap<>();
            }

            Map<Long, BigDecimal> odds = new HashMap<>();
            odds.put(match.getTeam1().getId(), match.getTeam1Odds());
            odds.put(match.getTeam2().getId(), match.getTeam2Odds());

            return odds;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting current odds for match: " + matchId, e);
            return new HashMap<>();
        }
    }

    @Override
    public BigDecimal getMinimumBetAmount() {
        return MIN_BET_AMOUNT;
    }

    @Override
    public BigDecimal getMaximumBetAmount() {
        return MAX_BET_AMOUNT;
    }

    @Override
    public BigDecimal getUserMaxBetAmount(Long userId) {
        return MAX_BET_AMOUNT;
    }

    @Override
    public boolean isWithinBetLimits(Long userId, BigDecimal amount) {
        return amount.compareTo(getMinimumBetAmount()) >= 0 &&
                amount.compareTo(getUserMaxBetAmount(userId)) <= 0;
    }

    @Override
    public List<Bet> getAllBets() {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery("SELECT b FROM Bet b ORDER BY b.betPlacedAt DESC", Bet.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all bets", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Bet> getBetsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            TypedQuery<Bet> query = entityManager.createQuery(
                    "SELECT b FROM Bet b WHERE b.betPlacedAt BETWEEN :startDate AND :endDate ORDER BY b.betPlacedAt DESC",
                    Bet.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting bets by date range", e);
            return List.of();
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    @Override
    public void processAllPendingBets() {
        try {
            List<Bet> pendingBets = getPendingBets();
            Map<Long, List<Bet>> betsByMatch = new HashMap<>();

            // Group bets by match
            for (Bet bet : pendingBets) {
                Long matchId = bet.getMatch().getId();
                betsByMatch.computeIfAbsent(matchId, k -> List.of()).add(bet);
            }

            // Process each match's bets
            MatchService matchSvc = getMatchService();
            for (Map.Entry<Long, List<Bet>> entry : betsByMatch.entrySet()) {
                Long matchId = entry.getKey();
                Match match = matchSvc.findMatchById(matchId);

                if (match != null && match.isCompleted()) {
                    processBetResults(matchId);
                }
            }

            logger.info("Processed all pending bets");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing all pending bets", e);
        }
    }

    @Override
    public void recalculateAllOdds() {
        try {
            MatchService matchSvc = getMatchService();
            List<Match> bettableMatches = matchSvc.getBettableMatches();
            for (Match match : bettableMatches) {
                updateMatchOdds(match.getId());
            }
            logger.info("Recalculated odds for " + bettableMatches.size() + " matches");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error recalculating all odds", e);
        }
    }

    @Override
    public Map<String, Object> getBettingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();

            // Total bets
            TypedQuery<Long> totalBetsQuery = entityManager.createQuery("SELECT COUNT(b) FROM Bet b", Long.class);
            stats.put("totalBets", totalBetsQuery.getSingleResult());

            // Total bet amount
            TypedQuery<BigDecimal> totalAmountQuery = entityManager.createQuery(
                    "SELECT COALESCE(SUM(b.betAmount), 0) FROM Bet b", BigDecimal.class);
            stats.put("totalBetAmount", totalAmountQuery.getSingleResult());

            // Pending bets
            TypedQuery<Long> pendingBetsQuery = entityManager.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.status = 'PENDING'", Long.class);
            stats.put("pendingBets", pendingBetsQuery.getSingleResult());

            // Won bets
            TypedQuery<Long> wonBetsQuery = entityManager.createQuery(
                    "SELECT COUNT(b) FROM Bet b WHERE b.status = 'WON'", Long.class);
            stats.put("wonBets", wonBetsQuery.getSingleResult());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting betting statistics", e);
        } finally {
            if (em == null && entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return stats;
    }

    @Override
    public Map<String, Object> getMatchBettingReport(Long matchId) {
        Map<String, Object> report = new HashMap<>();
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            if (match != null) {
                report.put("match", match);
                report.put("totalBets", getTotalBetsCount(matchId));
                report.put("totalAmount", getTotalBetAmount(matchId));
                report.put("team1Bets", getTeamBetsCount(matchId, match.getTeam1().getId()));
                report.put("team2Bets", getTeamBetsCount(matchId, match.getTeam2().getId()));
                report.put("team1Amount", getTeamBetAmount(matchId, match.getTeam1().getId()));
                report.put("team2Amount", getTeamBetAmount(matchId, match.getTeam2().getId()));
                report.put("currentOdds", getCurrentOddsForMatch(matchId));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting match betting report: " + matchId, e);
        }
        return report;
    }

    @Override
    public Map<String, Object> getUserBettingReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        try {
            UserService userSvc = getUserService();
            User user = userSvc.findUserById(userId);
            if (user != null) {
                report.put("user", user);
                report.put("totalBets", userSvc.getTotalBetsPlaced(userId));
                report.put("totalBetAmount", getUserTotalBetAmount(userId));
                report.put("totalWinnings", getUserTotalWinnings(userId));
                report.put("winRate", getUserWinRate(userId));
                report.put("pendingBets", getUserPendingBets(userId).size());
                report.put("wonBets", userSvc.getWonBetsCount(userId));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user betting report: " + userId, e);
        }
        return report;
    }

    @Override
    public List<Map<String, Object>> getTopBettors(int limit) {
        // Basic implementation - could be enhanced with actual query
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getPopularMatches(int limit) {
        // Basic implementation - could be enhanced with actual query
        return List.of();
    }

    @Override
    public BigDecimal calculateBookmakerMargin(Long matchId) {
        try {
            Map<Long, BigDecimal> odds = getCurrentOddsForMatch(matchId);
            if (odds.size() != 2) {
                return BigDecimal.ZERO;
            }

            BigDecimal impliedProbSum = BigDecimal.ZERO;
            for (BigDecimal odd : odds.values()) {
                impliedProbSum = impliedProbSum.add(BigDecimal.ONE.divide(odd, 4, BigDecimal.ROUND_HALF_UP));
            }

            return impliedProbSum.subtract(BigDecimal.ONE).multiply(new BigDecimal("100"));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating bookmaker margin for match: " + matchId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public void adjustOddsForRiskManagement(Long matchId) {
        updateMatchOdds(matchId);
    }

    @Override
    public boolean isMatchAtRiskLimit(Long matchId) {
        BigDecimal totalPool = getTotalBetAmount(matchId);
        BigDecimal riskLimit = new BigDecimal("50000.00"); // Example limit
        return totalPool.compareTo(riskLimit) > 0;
    }

    @Override
    public boolean isLiveBettingEnabled(Long matchId) {
        MatchService matchSvc = getMatchService();
        Match match = matchSvc.findMatchById(matchId);
        return match != null && match.isLive() && match.getBettingEnabled();
    }

    @Override
    public void enableLiveBetting(Long matchId) {
        MatchService matchSvc = getMatchService();
        matchSvc.enableBetting(matchId);
    }

    @Override
    public void disableLiveBetting(Long matchId) {
        MatchService matchSvc = getMatchService();
        matchSvc.disableBetting(matchId);
    }

    @Override
    public void updateLiveOdds(Long matchId, Map<Long, BigDecimal> newOdds) {
        try {
            MatchService matchSvc = getMatchService();
            Match match = matchSvc.findMatchById(matchId);
            if (match != null && newOdds.size() == 2) {
                BigDecimal team1Odds = newOdds.get(match.getTeam1().getId());
                BigDecimal team2Odds = newOdds.get(match.getTeam2().getId());

                if (team1Odds != null && team2Odds != null) {
                    matchSvc.updateOdds(matchId, team1Odds, team2Odds);
                    logger.info("Live odds updated for match: " + matchId);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating live odds for match: " + matchId, e);
        }
    }
}