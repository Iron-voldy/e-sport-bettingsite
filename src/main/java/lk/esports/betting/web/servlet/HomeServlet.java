package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.entity.Match;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet(name = "HomeServlet", urlPatterns = {"", "/"})
public class HomeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(HomeServlet.class.getName());

    @EJB
    private MatchService matchService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Get featured matches for home page
            List<Match> featuredMatches = null;
            List<Match> liveMatches = null;

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
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading matches for home page", e);
                    // Continue with empty lists
                }
            } else {
                logger.warning("MatchService is null - EJB injection may have failed");
            }

            // Set attributes for JSP
            request.setAttribute("featuredMatches", featuredMatches != null ? featuredMatches : List.of());
            request.setAttribute("liveMatches", liveMatches != null ? liveMatches : List.of());

            // Forward to home page
            request.getRequestDispatcher("/index.jsp").forward(request, response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in HomeServlet", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect POST requests to GET
        doGet(request, response);
    }
}