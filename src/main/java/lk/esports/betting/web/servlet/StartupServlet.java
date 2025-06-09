package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.utils.DatabaseUtil;
import lk.esports.betting.utils.EJBServiceLocator;
import lk.esports.betting.entity.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet(name = "StartupServlet", urlPatterns = {"/startup"}, loadOnStartup = 1)
public class StartupServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(StartupServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("🚀 Starting E-Sports Betting Platform initialization...");

        try {
            // 1. Test database connection first
            boolean dbHealthy = DatabaseUtil.isDatabaseHealthy();
            logger.info("📊 Database health: " + (dbHealthy ? "✅ HEALTHY" : "❌ UNHEALTHY"));

            if (!dbHealthy) {
                logger.warning("⚠️ Database is not healthy. Skipping data initialization.");
                return;
            }

            // 2. Wait a moment for services to be available
            Thread.sleep(2000);

            // 3. Get services using service locator (EJB injection might not work during startup)
            MatchService matchService = null;
            UserService userService = null;

            try {
                matchService = EJBServiceLocator.getMatchService();
                userService = EJBServiceLocator.getUserService();
                logger.info("🔧 Services loaded successfully");
            } catch (Exception e) {
                logger.warning("⚠️ Services not available during startup: " + e.getMessage());
                logger.info("🔄 Services will be initialized on first request");
                return;
            }

            // 4. Initialize sample data if services are available
            if (matchService != null && userService != null) {
                initializeSampleData(matchService, userService);
                logger.info("✅ Sample data initialization completed successfully");
            }

            // 5. Test service functionality
            testServices(matchService, userService);

            logger.info("🎉 E-Sports Betting Platform initialization completed successfully!");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error during application initialization", e);
            // Don't throw exception - let the app start anyway
        }
    }

    private void initializeSampleData(MatchService matchService, UserService userService) {
        try {
            logger.info("🎯 Initializing sample data...");

            // Create sample teams if they don't exist
            createSampleTeams(matchService);

            // Create sample tournament
            createSampleTournament(matchService);

            // Create sample matches
            createSampleMatches(matchService);

            // Create demo user if doesn't exist
            createDemoUser(userService);

            logger.info("✨ Sample data created successfully");

        } catch (Exception e) {
            logger.log(Level.WARNING, "⚠️ Error creating sample data", e);
        }
    }

    private void createSampleTeams(MatchService matchService) {
        try {
            // Check if teams already exist
            var existingTeams = matchService.getAllTeams();
            if (existingTeams != null && !existingTeams.isEmpty()) {
                logger.info("📋 Teams already exist (" + existingTeams.size() + " teams found)");
                return;
            }

            logger.info("👥 Creating sample teams...");

            // Create sample teams with different countries and realistic stats
            String[][] teamsData = {
                    {"Blacklist International", "BLI", "Philippines", "Top-tier MPL Philippines team, multiple championship winners"},
                    {"RRQ Hoshi", "RRQ", "Indonesia", "Indonesian powerhouse team with strong international presence"},
                    {"ONIC Esports", "ONIC", "Philippines", "One of the strongest teams in MPL Philippines"},
                    {"EVOS Legends", "EVOS", "Indonesia", "Legendary Indonesian team with rich tournament history"},
                    {"Echo", "ECHO", "Philippines", "Rising stars in the Mobile Legends scene"},
                    {"Team Flash", "FLASH", "Philippines", "Fast-paced aggressive playstyle team"}
            };

            for (String[] teamData : teamsData) {
                try {
                    Team team = matchService.createTeam(
                            teamData[0], // name
                            teamData[1], // code
                            teamData[2], // country
                            null,        // logoUrl
                            teamData[3]  // description
                    );

                    if (team != null) {
                        logger.info("✅ Created team: " + team.getTeamName() + " (" + team.getTeamCode() + ")");
                    }
                } catch (Exception e) {
                    logger.warning("⚠️ Failed to create team " + teamData[0] + ": " + e.getMessage());
                }
            }

            logger.info("👥 Sample teams creation completed");

        } catch (Exception e) {
            logger.log(Level.WARNING, "❌ Error creating sample teams", e);
        }
    }

    private void createSampleTournament(MatchService matchService) {
        try {
            var existingTournaments = matchService.getAllTournaments();
            if (existingTournaments != null && !existingTournaments.isEmpty()) {
                logger.info("🏆 Tournaments already exist (" + existingTournaments.size() + " tournaments found)");
                return;
            }

            logger.info("🏆 Creating sample tournament...");

            Tournament tournament = matchService.createTournament(
                    "MPL Season 12",
                    Tournament.TournamentType.REGULAR,
                    LocalDate.now().minusDays(30),
                    LocalDate.now().plusDays(60),
                    new BigDecimal("500000"),
                    "Mobile Legends Professional League Season 12 - The ultimate championship"
            );

            if (tournament != null) {
                logger.info("✅ Created tournament: " + tournament.getTournamentName());
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "❌ Error creating sample tournament", e);
        }
    }

    private void createSampleMatches(MatchService matchService) {
        try {
            var existingMatches = matchService.getAllMatches();
            if (existingMatches != null && !existingMatches.isEmpty()) {
                logger.info("⚽ Matches already exist (" + existingMatches.size() + " matches found)");
                return;
            }

            logger.info("⚽ Creating sample matches...");

            var tournaments = matchService.getAllTournaments();
            var teams = matchService.getAllTeams();

            if (tournaments.isEmpty() || teams.size() < 4) {
                logger.warning("⚠️ Cannot create matches: insufficient tournaments or teams");
                return;
            }

            Tournament tournament = tournaments.get(0);

            // Create multiple upcoming matches with different times
            String[][] matchups = {
                    {"0", "1", "2", "BO3"},   // BLI vs RRQ in 2 hours
                    {"2", "3", "4", "BO3"},   // ONIC vs EVOS in 4 hours
                    {"4", "5", "6", "BO1"},   // Echo vs Flash in 6 hours
                    {"0", "2", "8", "BO3"},   // BLI vs ONIC in 8 hours
                    {"1", "3", "12", "BO5"}   // RRQ vs EVOS in 12 hours
            };

            for (String[] matchup : matchups) {
                try {
                    int team1Index = Integer.parseInt(matchup[0]);
                    int team2Index = Integer.parseInt(matchup[1]);
                    int hoursFromNow = Integer.parseInt(matchup[2]);
                    Match.MatchType matchType = Match.MatchType.valueOf(matchup[3]);

                    if (team1Index < teams.size() && team2Index < teams.size()) {
                        Match match = matchService.createMatch(
                                tournament.getId(),
                                teams.get(team1Index).getId(),
                                teams.get(team2Index).getId(),
                                LocalDateTime.now().plusHours(hoursFromNow),
                                matchType
                        );

                        if (match != null) {
                            logger.info("✅ Created match: " + match.getMatchTitle() +
                                    " (" + matchType + ") in " + hoursFromNow + " hours");
                        }
                    }
                } catch (Exception e) {
                    logger.warning("⚠️ Failed to create match: " + e.getMessage());
                }
            }

            // Create one completed match for testing
            try {
                if (teams.size() >= 2) {
                    Match completedMatch = matchService.createMatch(
                            tournament.getId(),
                            teams.get(0).getId(),
                            teams.get(1).getId(),
                            LocalDateTime.now().minusHours(24),
                            Match.MatchType.BO3
                    );

                    if (completedMatch != null) {
                        // Complete the match
                        matchService.completeMatch(
                                completedMatch.getId(),
                                teams.get(0).getId(), // BLI wins
                                2, 1 // Score 2-1
                        );
                        logger.info("✅ Created completed match: " + completedMatch.getMatchTitle());
                    }
                }
            } catch (Exception e) {
                logger.warning("⚠️ Failed to create completed match: " + e.getMessage());
            }

            logger.info("⚽ Sample matches creation completed");

        } catch (Exception e) {
            logger.log(Level.WARNING, "❌ Error creating sample matches", e);
        }
    }

    private void createDemoUser(UserService userService) {
        try {
            if (userService.isEmailExists("demo@mlbetting.com")) {
                logger.info("👤 Demo user already exists");
                return;
            }

            logger.info("👤 Creating demo user...");

            User demoUser = userService.registerUser(
                    "demo@mlbetting.com",
                    "demo_user",
                    "Demo123!@#",
                    "Demo User",
                    "+1234567890"
            );

            if (demoUser != null) {
                // Add initial balance to demo user
                userService.addFunds(demoUser.getId(), new BigDecimal("1000.00"),
                        "Demo account initial balance");

                logger.info("✅ Demo user created successfully with $1000 initial balance");
                logger.info("📧 Demo login: demo@mlbetting.com / Demo123!@#");
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "❌ Error creating demo user", e);
        }
    }

    private void testServices(MatchService matchService, UserService userService) {
        try {
            logger.info("🧪 Testing services...");

            // Test MatchService
            if (matchService != null) {
                var upcomingMatches = matchService.getUpcomingMatches();
                logger.info("📊 Found " + (upcomingMatches != null ? upcomingMatches.size() : 0) + " upcoming matches");

                var allTeams = matchService.getAllTeams();
                logger.info("👥 Found " + (allTeams != null ? allTeams.size() : 0) + " teams");
            }

            // Test UserService
            if (userService != null) {
                var activeUsers = userService.getActiveUsers();
                logger.info("👤 Found " + (activeUsers != null ? activeUsers.size() : 0) + " active users");
            }

            logger.info("✅ Service tests completed successfully");

        } catch (Exception e) {
            logger.log(Level.WARNING, "⚠️ Error during service testing", e);
        }
    }
}