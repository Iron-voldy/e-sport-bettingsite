package lk.esports.betting.ejb.local;

import lk.esports.betting.entity.Bet;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.User;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Local
public interface BettingService {

    // Bet Placement
    Bet placeBet(Long userId, Long matchId, Long selectedTeamId, BigDecimal betAmount);
    boolean validateBet(Long userId, Long matchId, Long selectedTeamId, BigDecimal betAmount);
    BigDecimal calculatePotentialWinnings(Long matchId, Long selectedTeamId, BigDecimal betAmount);

    // Bet Management
    Bet findBetById(Long betId);
    List<Bet> getUserBets(Long userId);
    List<Bet> getMatchBets(Long matchId);
    List<Bet> getBetsByStatus(Bet.BetStatus status);
    List<Bet> getUserBetsByMatch(Long userId, Long matchId);
    List<Bet> getPendingBets();

    // Bet Processing
    void processBetResults(Long matchId);
    void markBetAsWon(Long betId);
    void markBetAsLost(Long betId);
    void cancelBet(Long betId);
    void refundBet(Long betId);

    // Bet Validation
    boolean canPlaceBet(Long userId, Long matchId, BigDecimal amount);
    boolean isValidBetAmount(BigDecimal amount);
    boolean hasUserBetOnMatch(Long userId, Long matchId);
    boolean isMatchBettable(Long matchId);

    // Bet Statistics
    BigDecimal getTotalBetAmount(Long matchId);
    BigDecimal getTeamBetAmount(Long matchId, Long teamId);
    int getTotalBetsCount(Long matchId);
    int getTeamBetsCount(Long matchId, Long teamId);
    Map<Long, BigDecimal> getTeamBetDistribution(Long matchId);

    // User Betting History
    List<Bet> getUserWinningBets(Long userId);
    List<Bet> getUserLosingBets(Long userId);
    List<Bet> getUserPendingBets(Long userId);
    BigDecimal getUserTotalBetAmount(Long userId);
    BigDecimal getUserTotalWinnings(Long userId);
    double getUserWinRate(Long userId);

    // Odds Calculation
    BigDecimal calculateCurrentOdds(Long matchId, Long teamId);
    void updateMatchOdds(Long matchId);
    Map<Long, BigDecimal> getCurrentOddsForMatch(Long matchId);

    // Bet Limits and Rules
    BigDecimal getMinimumBetAmount();
    BigDecimal getMaximumBetAmount();
    BigDecimal getUserMaxBetAmount(Long userId);
    boolean isWithinBetLimits(Long userId, BigDecimal amount);

    // Administrative Functions
    List<Bet> getAllBets();
    List<Bet> getBetsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    void processAllPendingBets();
    void recalculateAllOdds();

    // Reporting
    Map<String, Object> getBettingStatistics();
    Map<String, Object> getMatchBettingReport(Long matchId);
    Map<String, Object> getUserBettingReport(Long userId);
    List<Map<String, Object>> getTopBettors(int limit);
    List<Map<String, Object>> getPopularMatches(int limit);

    // Risk Management
    BigDecimal calculateBookmakerMargin(Long matchId);
    void adjustOddsForRiskManagement(Long matchId);
    boolean isMatchAtRiskLimit(Long matchId);

    // Live Betting Features
    boolean isLiveBettingEnabled(Long matchId);
    void enableLiveBetting(Long matchId);
    void disableLiveBetting(Long matchId);
    void updateLiveOdds(Long matchId, Map<Long, BigDecimal> newOdds);
}