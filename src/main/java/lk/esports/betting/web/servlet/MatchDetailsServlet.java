package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Bet;
import lk.esports.betting.utils.EJBServiceLocator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/matches/details/*")
public class MatchDetailsServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MatchDetailsServlet.class.getName());

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Match ID is required");
            return;
        }

        try {
            // Extract match ID from path
            String matchIdStr = pathInfo.substring(1); // Remove leading "/"
            Long matchId = Long.parseLong(matchIdStr);

            // Get services
            MatchService matchService = EJBServiceLocator.getMatchService();
            BettingService bettingService = EJBServiceLocator.getBettingService();
            UserService userService = EJBServiceLocator.getUserService();

            if (matchService == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Match service unavailable");
                return;
            }

            // Find the match
            Match match = matchService.findMatchById(matchId);
            if (match == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Match not found");
                return;
            }

            // Get betting statistics for the match
            Map<String, Object> bettingReport = new HashMap<>();
            if (bettingService != null) {
                try {
                    bettingReport = bettingService.getMatchBettingReport(matchId);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error getting betting report for match: " + matchId, e);
                    bettingReport = new HashMap<>();
                }
            }

            // Get user's bets on this match if logged in
            List<Bet> userBetsOnMatch = null;
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null && bettingService != null) {
                try {
                    Long userId = (Long) session.getAttribute("userId");
                    userBetsOnMatch = bettingService.getUserBetsByMatch(userId, matchId);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error getting user bets for match: " + matchId, e);
                }
            }

            // Check for JSON response
            String format = request.getParameter("format");
            if ("json".equals(format)) {
                Map<String, Object> matchData = new HashMap<>();
                matchData.put("match", match);
                matchData.put("bettingStats", bettingReport);
                if (userBetsOnMatch != null) {
                    matchData.put("userBets", userBetsOnMatch);
                }
                sendJsonResponse(response, matchData);
            } else {
                // Forward to JSP
                request.setAttribute("match", match);
                request.setAttribute("bettingStats", bettingReport);
                request.setAttribute("userBetsOnMatch", userBetsOnMatch);
                request.getRequestDispatcher("/match-details.jsp").forward(request, response);
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid match ID");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting match details", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading match details");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }
}