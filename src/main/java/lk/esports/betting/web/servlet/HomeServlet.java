package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.entity.Match;
import lk.esports.betting.utils.DatabaseUtil;
import lk.esports.betting.utils.EJBServiceLocator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet(name = "HomeServlet", urlPatterns = {"", "/"})
public class HomeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(HomeServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("HomeServlet initializing...");

        // Test database connection
        try {
            boolean dbHealthy = DatabaseUtil.isDatabaseHealthy();
            logger.info("Database health check: " + (dbHealthy ? "HEALTHY" : "UNHEALTHY"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database health check failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<Match> featuredMatches = new ArrayList<>();
            List<Match> liveMatches = new ArrayList<>();

            // Get MatchService using service locator
            MatchService matchService = EJBServiceLocator.getMatchService();

            if (matchService != null) {
                try {
                    featuredMatches = matchService.getUpcomingMatches();
                    liveMatches = matchService.getLiveMatches();

                    // Limit to first 6 matches for home page
                    if (featuredMatches != null && featuredMatches.size() > 6) {
                        featuredMatches = featuredMatches.subList(0, 6);
                    }

                    if (liveMatches != null && liveMatches.size() > 3) {
                        liveMatches = liveMatches.subList(0, 3);
                    }

                    logger.info("Successfully loaded matches: " +
                            (featuredMatches != null ? featuredMatches.size() : 0) + " featured, " +
                            (liveMatches != null ? liveMatches.size() : 0) + " live");

                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading matches from service", e);
                    // Set empty lists but don't fail
                    featuredMatches = new ArrayList<>();
                    liveMatches = new ArrayList<>();
                }
            } else {
                logger.warning("MatchService is null - service locator failed");
                // Create empty lists to avoid JSP errors
                featuredMatches = new ArrayList<>();
                liveMatches = new ArrayList<>();
            }

            // Set attributes for JSP (always set, even if empty)
            request.setAttribute("featuredMatches", featuredMatches);
            request.setAttribute("liveMatches", liveMatches);

            // Add some debug info for the JSP
            request.setAttribute("serviceStatus", matchService != null ? "available" : "unavailable");
            request.setAttribute("dbStatus", DatabaseUtil.isDatabaseHealthy() ? "healthy" : "unhealthy");

            // Forward to home page
            request.getRequestDispatcher("/index.jsp").forward(request, response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in HomeServlet", e);

            // Set minimal attributes to prevent JSP errors
            request.setAttribute("featuredMatches", new ArrayList<>());
            request.setAttribute("liveMatches", new ArrayList<>());
            request.setAttribute("serviceStatus", "error");
            request.setAttribute("dbStatus", "error");
            request.setAttribute("errorMessage", "System is starting up. Please refresh in a moment.");

            // Still forward to JSP, don't send error
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect POST requests to GET
        doGet(request, response);
    }
}