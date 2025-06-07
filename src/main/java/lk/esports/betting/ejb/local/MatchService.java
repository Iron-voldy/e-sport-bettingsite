package lk.esports.betting.ejb.local;

import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Team;
import lk.esports.betting.entity.Tournament;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Local
public interface MatchService {

    // Match Management
    Match createMatch(Long tournamentId, Long team1Id, Long team2Id, LocalDateTime matchDate,
                      Match.MatchType matchType);
    Match findMatchById(Long matchId);
    void updateMatch(Match match);
    void deleteMatch(Long matchId);

    // Match Queries
    List<Match> getAllMatches();
    List<Match> getUpcomingMatches();
    List<Match> getLiveMatches();
    List<Match> getCompletedMatches();
    List<Match> getMatchesByTournament(Long tournamentId);
    List<Match> getBettableMatches();
    List<Match> getMatchesByTeam(Long teamId);
    List<Match> getMatchesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Match Status Management
    void startMatch(Long matchId);
    void completeMatch(Long matchId, Long winnerTeamId, int team1Score, int team2Score);
    void cancelMatch(Long matchId);
    void enableBetting(Long matchId);
    void disableBetting(Long matchId);

    // Odds Management
    void updateOdds(Long matchId, BigDecimal team1Odds, BigDecimal team2Odds);
    BigDecimal calculateDynamicOdds(Long matchId, Long teamId);
    void recalculateOdds(Long matchId);

    // Team Management
    Team createTeam(String teamName, String teamCode, String country, String logoUrl, String description);
    Team findTeamById(Long teamId);
    Team findTeamByCode(String teamCode);
    List<Team> getAllTeams();
    List<Team> getActiveTeams();
    List<Team> getTeamsByCountry(String country);
    void updateTeam(Team team);
    void updateTeamStats(Long teamId);

    // Tournament Management
    Tournament createTournament(String tournamentName, Tournament.TournamentType type,
                                java.time.LocalDate startDate, java.time.LocalDate endDate,
                                BigDecimal prizePool, String description);
    Tournament findTournamentById(Long tournamentId);
    List<Tournament> getAllTournaments();
    List<Tournament> getActiveTournaments();
    List<Tournament> getUpcomingTournaments();
    List<Tournament> getCompletedTournaments();
    void updateTournament(Tournament tournament);
    void startTournament(Long tournamentId);
    void completeTournament(Long tournamentId);

    // Match Statistics
    BigDecimal getTotalBetPool(Long matchId);
    BigDecimal getTeamBetPool(Long matchId, Long teamId);
    int getTotalBetsCount(Long matchId);
    int getTeamBetsCount(Long matchId, Long teamId);

    // Validation
    boolean isMatchBettable(Long matchId);
    boolean canUpdateMatch(Long matchId);
    boolean isValidMatchup(Long team1Id, Long team2Id);

    // Search and Filter
    List<Match> searchMatches(String keyword);
    List<Match> filterMatchesByStatus(Match.MatchStatus status);
    List<Match> filterMatchesByType(Match.MatchType type);
}