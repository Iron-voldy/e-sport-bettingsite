package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.utils.DatabaseUtil;
import lk.esports.betting.entity.*;

import jakarta.ejb.EJB;
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

    @EJB
    private MatchService matchService;

    @EJB
    private UserService userService;

    @EJB
    private BettingService bettingService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("Starting application initialization...");

        try {
            // Test database connection first
            boolean dbHealthy = DatabaseUtil.isDatabaseHealthy();
            logger.info("Database health: " + (dbHealthy ? "HEALTHY" : "UNHEALTHY"));

            if (!dbHealthy) {
                logger.warning("Database is not healthy. Skipping data initialization.");
                return;
            }

            // Initialize sample data if EJBs are available
            if (matchService != null && userService != null) {
                initializeSampleData();
                logger.info("Sample data initialization completed successfully");
            } else {
                logger.warning("EJB services not available during startup. Will be initialized on first request.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during application initialization", e);
            // Don't throw exception - let the app start anyway
        }
    }

    private void initializeSampleData() {
        try {
            // Create sample teams if they don't exist
            createSampleTeams();

            // Create sample tournament
            createSampleTournament();

            // Create sample matches
            createSampleMatches();

            // Create demo user if doesn't exist
            createDemoUser();

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating sample data", e);
        }
    }

    private void createSampleTeams() {
        try {
            // Check if teams already exist
            if (matchService.getAllTeams().isEmpty()) {
                logger.info("Creating sample teams...");

                matchService.createTeam("Blacklist International", "BLI", "Philippines",
                        null, "Professional Mobile Legends team from the Philippines");

                matchService.createTeam("RRQ Hoshi", "RRQ", "Indonesia",
                        null, "Indonesian powerhouse team");

                matchService.createTeam("ONIC Esports", "ONIC", "Philippines",
                        null, "One of the strongest teams in MPL Philippines");

                matchService.createTeam("EVOS Legends", "EVOS", "Indonesia",
                        null, "Legendary Indonesian team");

                matchService.createTeam("Echo", "ECHO", "Philippines",
                        null, "Rising stars in the Mobile Legends scene");

                matchService.createTeam("Team Flash", "FLASH", "Philippines",
                        null, "Fast-paced aggressive playstyle team");

                logger.info("Sample teams created successfully");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating sample teams", e);
        }
    }

    private void createSampleTournament() {
        try {
            if (matchService.getAllTournaments().isEmpty()) {
                logger.info("Creating sample tournament...");

                Tournament tournament = matchService.createTournament(
                        "MPL Season 12",
                        Tournament.TournamentType.REGULAR,
                        LocalDate.now().minusDays(30),
                        LocalDate.now().plusDays(60),
                        new BigDecimal("500000"),
                        "Mobile Legends Professional League Season 12"
                );

                if (tournament != null) {
                    logger.info("Sample tournament created: " + tournament.getTournamentName());
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating sample tournament", e);
        }
    }

    private void createSampleMatches() {
        try {
            if (matchService.getAllMatches().isEmpty()) {
                logger.info("Creating sample matches...");

                var tournaments = matchService.getAllTournaments();
                var teams = matchService.getAllTeams();

                if (!tournaments.isEmpty() && teams.size() >= 4) {
                    Tournament tournament = tournaments.get(0);

                    // Create upcoming matches
                    matchService.createMatch(
                            tournament.getId(),
                            teams.get(0).getId(), // Blacklist
                            teams.get(1).getId(), // RRQ
                            LocalDateTime.now().plusHours(2),
                            Match.MatchType.BO3
                    );

                    matchService.createMatch(
                            tournament.getId(),
                            teams.get(2).getId(), // ONIC
                            teams.get(3).getId(), // EVOS
                            LocalDateTime.now().plusHours(4),
                            Match.MatchType.BO3
                    );

                    if (teams.size() >= 6) {
                        matchService.createMatch(
                                tournament.getId(),
                                teams.get(4).getId(), // Echo
                                teams.get(5).getId(), // Team Flash
                                LocalDateTime.now().plusHours(6),
                                Match.MatchType.BO1
                        );
                    }

                    logger.info("Sample matches created successfully");
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating sample matches", e);
        }
    }

    private void createDemoUser() {
        try {
            if (!userService.isEmailExists("demo@mlbetting.com")) {
                logger.info("Creating demo user...");

                User demoUser = userService.registerUser(
                        "demo@mlbetting.com",
                        "demo_user",
                        "Demo123!@#",
                        "Demo User",
                        "+1234567890"
                );

                if (demoUser != null) {
                    // Add some balance to demo user
                    userService.addFunds(demoUser.getId(), new BigDecimal("1000.00"), "Demo account initial balance");
                    logger.info("Demo user created successfully with initial balance");
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating demo user", e);
        }
    }
}