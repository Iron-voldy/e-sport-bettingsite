package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Team;
import lk.esports.betting.entity.Tournament;
import lk.esports.betting.utils.EJBServiceLocator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/matches/*")
public class MatchServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MatchServlet.class.getName());

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all matches
                handleGetAllMatches(request, response);
            } else if (pathInfo.equals("/upcoming")) {
                handleGetUpcomingMatches(request, response);
            } else if (pathInfo.equals("/live")) {
                handleGetLiveMatches(request, response);
            } else if (pathInfo.equals("/completed")) {
                handleGetCompletedMatches(request, response);
            } else if (pathInfo.startsWith("/details/")) {
                handleGetMatchDetails(request, response, pathInfo);
            } else if (pathInfo.startsWith("/bets/")) {
                handleGetMatchBets(request, response, pathInfo);
            } else if (pathInfo.equals("/teams")) {
                handleGetTeams(request, response);
            } else if (pathInfo.equals("/tournaments")) {
                handleGetTournaments(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in MatchServlet GET", e);
            request.setAttribute("errorMessage", "Error loading match data. Please try again.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/create")) {
                handleCreateMatch(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/update/")) {
                handleUpdateMatch(request, response, pathInfo);
            } else if (pathInfo != null && pathInfo.startsWith("/start/")) {
                handleStartMatch(request, response, pathInfo);
            } else if (pathInfo != null && pathInfo.startsWith("/complete/")) {
                handleCompleteMatch(request, response, pathInfo);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in MatchServlet POST", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void handleGetAllMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            MatchService matchService = EJBServiceLocator.getMatchService();
            if (matchService == null) {
                request.setAttribute("errorMessage", "Match service is not available");
                request.setAttribute("matches", List.of());
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
                return;
            }

            String format = request.getParameter("format");
            List<Match> matches = matchService.getAllMatches();

            if ("json".equals(format)) {
                sendJsonResponse(response, matches != null ? matches : List.of());
            } else {
                request.setAttribute("matches", matches != null ? matches : List.of());
                request.setAttribute("pageTitle", "All Matches");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all matches", e);
            request.setAttribute("errorMessage", "Error loading matches. Please try again.");
            request.setAttribute("matches", List.of());
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetUpcomingMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            MatchService matchService = EJBServiceLocator.getMatchService();
            if (matchService == null) {
                request.setAttribute("errorMessage", "Match service is not available");
                request.setAttribute("matches", List.of());
                request.setAttribute("pageTitle", "Upcoming Matches");
                request.setAttribute("matchType", "upcoming");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
                return;
            }

            String format = request.getParameter("format");
            List<Match> matches = matchService.getUpcomingMatches();

            if ("json".equals(format)) {
                sendJsonResponse(response, matches != null ? matches : List.of());
            } else {
                request.setAttribute("matches", matches != null ? matches : List.of());
                request.setAttribute("pageTitle", "Upcoming Matches");
                request.setAttribute("matchType", "upcoming");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming matches", e);
            request.setAttribute("errorMessage", "Error loading upcoming matches. Please try again.");
            request.setAttribute("matches", List.of());
            request.setAttribute("pageTitle", "Upcoming Matches");
            request.setAttribute("matchType", "upcoming");
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetLiveMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            MatchService matchService = EJBServiceLocator.getMatchService();
            if (matchService == null) {
                request.setAttribute("errorMessage", "Match service is not available");
                request.setAttribute("matches", List.of());
                request.setAttribute("pageTitle", "Live Matches");
                request.setAttribute("matchType", "live");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
                return;
            }

            String format = request.getParameter("format");
            List<Match> matches = matchService.getLiveMatches();

            if ("json".equals(format)) {
                sendJsonResponse(response, matches != null ? matches : List.of());
            } else {
                request.setAttribute("matches", matches != null ? matches : List.of());
                request.setAttribute("pageTitle", "Live Matches");
                request.setAttribute("matchType", "live");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting live matches", e);
            request.setAttribute("errorMessage", "Error loading live matches. Please try again.");
            request.setAttribute("matches", List.of());
            request.setAttribute("pageTitle", "Live Matches");
            request.setAttribute("matchType", "live");
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetCompletedMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            MatchService matchService = EJBServiceLocator.getMatchService();
            if (matchService == null) {
                request.setAttribute("errorMessage", "Match service is not available");
                request.setAttribute("matches", List.of());
                request.setAttribute("pageTitle", "Completed Matches");
                request.setAttribute("matchType", "completed");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
                return;
            }

            String format = request.getParameter("format");
            List<Match> matches = matchService.getCompletedMatches();

            if ("json".equals(format)) {
                sendJsonResponse(response, matches != null ? matches : List.of());
            } else {
                request.setAttribute("matches", matches != null ? matches : List.of());
                request.setAttribute("pageTitle", "Completed Matches");
                request.setAttribute("matchType", "completed");
                request.getRequestDispatcher("/matches.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting completed matches", e);
            request.setAttribute("errorMessage", "Error loading completed matches. Please try again.");
            request.setAttribute("matches", List.of());
            request.setAttribute("pageTitle", "Completed Matches");
            request.setAttribute("matchType", "completed");
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetMatchDetails(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {

        try {
            String matchIdStr = pathInfo.substring("/details/".length());
            Long matchId = Long.parseLong(matchIdStr);

            MatchService matchService = EJBServiceLocator.getMatchService();
            BettingService bettingService = EJBServiceLocator.getBettingService();

            if (matchService == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Match service unavailable");
                return;
            }

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

            String format = request.getParameter("format");
            if ("json".equals(format)) {
                Map<String, Object> matchData = Map.of(
                        "match", match,
                        "bettingStats", bettingReport
                );
                sendJsonResponse(response, matchData);
            } else {
                request.setAttribute("match", match);
                request.setAttribute("bettingStats", bettingReport);
                request.getRequestDispatcher("/match-details.jsp").forward(request, response);
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid match ID");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting match details", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading match details");
        }
    }

    private void handleGetMatchBets(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {

        try {
            String matchIdStr = pathInfo.substring("/bets/".length());
            Long matchId = Long.parseLong(matchIdStr);

            MatchService matchService = EJBServiceLocator.getMatchService();
            BettingService bettingService = EJBServiceLocator.getBettingService();

            if (matchService == null || bettingService == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Services unavailable");
                return;
            }

            // Check if match exists
            Match match = matchService.findMatchById(matchId);
            if (match == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Match not found");
                return;
            }

            // Get betting report
            Map<String, Object> bettingReport = bettingService.getMatchBettingReport(matchId);
            sendJsonResponse(response, bettingReport);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid match ID");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting match bets", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading match betting data");
        }
    }

    private void handleGetTeams(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            MatchService matchService = EJBServiceLocator.getMatchService();
            if (matchService == null) {
                request.setAttribute("errorMessage", "Match service is not available");
                request.setAttribute("teams", List.of());
                request.getRequestDispatcher("/teams.jsp").forward(request, response);
                return;
            }

            List<Team> teams = matchService.getAllTeams();

            String format = request.getParameter("format");
            if ("json".equals(format)) {
                sendJsonResponse(response, teams != null ? teams : List.of());
            } else {
                request.setAttribute("teams", teams != null ? teams : List.of());
                request.getRequestDispatcher("/teams.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting teams", e);
            request.setAttribute("errorMessage", "Error loading teams. Please try again.");
            request.setAttribute("teams", List.of());
            request.getRequestDispatcher("/teams.jsp").forward(request, response);
        }
    }

    private void handleGetTournaments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            MatchService matchService = EJBServiceLocator.getMatchService();
            if (matchService == null) {
                request.setAttribute("errorMessage", "Match service is not available");
                request.setAttribute("tournaments", List.of());
                request.getRequestDispatcher("/tournaments.jsp").forward(request, response);
                return;
            }

            List<Tournament> tournaments = matchService.getAllTournaments();

            String format = request.getParameter("format");
            if ("json".equals(format)) {
                sendJsonResponse(response, tournaments != null ? tournaments : List.of());
            } else {
                request.setAttribute("tournaments", tournaments != null ? tournaments : List.of());
                request.getRequestDispatcher("/tournaments.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting tournaments", e);
            request.setAttribute("errorMessage", "Error loading tournaments. Please try again.");
            request.setAttribute("tournaments", List.of());
            request.getRequestDispatcher("/tournaments.jsp").forward(request, response);
        }
    }

    private void handleCreateMatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // This would be used by admin interface to create new matches
        // For now, return method not allowed for regular users
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Match creation not available");
    }

    private void handleUpdateMatch(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {

        // This would be used by admin interface to update matches
        // For now, return method not allowed for regular users
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Match update not available");
    }

    private void handleStartMatch(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {

        // This would be used by admin interface to start matches
        // For now, return method not allowed for regular users
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Match control not available");
    }

    private void handleCompleteMatch(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {

        // This would be used by admin interface to complete matches
        // For now, return method not allowed for regular users
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Match control not available");
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