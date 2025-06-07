package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Team;
import lk.esports.betting.entity.Tournament;

import jakarta.ejb.EJB;
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
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/matches/*")
public class MatchServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MatchServlet.class.getName());

    @EJB
    private MatchService matchService;

    @EJB
    private BettingService bettingService;

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
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
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

        String format = request.getParameter("format");
        List<Match> matches = matchService.getAllMatches();

        if ("json".equals(format)) {
            sendJsonResponse(response, matches);
        } else {
            request.setAttribute("matches", matches);
            request.setAttribute("pageTitle", "All Matches");
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetUpcomingMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        List<Match> matches = matchService.getUpcomingMatches();

        if ("json".equals(format)) {
            sendJsonResponse(response, matches);
        } else {
            request.setAttribute("matches", matches);
            request.setAttribute("pageTitle", "Upcoming Matches");
            request.setAttribute("matchType", "upcoming");
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetLiveMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        List<Match> matches = matchService.getLiveMatches();

        if ("json".equals(format)) {
            sendJsonResponse(response, matches);
        } else {
            request.setAttribute("matches", matches);
            request.setAttribute("pageTitle", "Live Matches");
            request.setAttribute("matchType", "live");
            request.getRequestDispatcher("/matches.jsp").forward(request, response);
        }
    }

    private void handleGetCompletedMatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        List<Match> matches = matchService.getCompletedMatches();

        if ("json".equals(format)) {
            sendJsonResponse(response, matches);
        } else {
            request.setAttribute("matches", matches);
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

            Match match = matchService.findMatchById(matchId);
            if (match == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Match not found");
                return;
            }

            // Get betting statistics for the match
            Map<String, Object> bettingReport = bettingService.getMatchBettingReport(matchId);

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
        }
    }

    private void handleGetMatchBets(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {

        try {
            String matchIdStr = pathInfo.substring("/bets/".length());
            Long matchId = Long.parseLong(matchIdStr);

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
        }
    }

    private void handleGetTeams(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Team> teams = matchService.getAllTeams();

        String format = request.getParameter("format");
        if ("json".equals(format)) {
            sendJsonResponse(response, teams);
        } else {
            request.setAttribute("teams", teams);
            request.getRequestDispatcher("/teams.jsp").forward(request, response);
        }
    }

    private void handleGetTournaments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Tournament> tournaments = matchService.getAllTournaments();

        String format = request.getParameter("format");
        if ("json".equals(format)) {
            sendJsonResponse(response, tournaments);
        } else {
            request.setAttribute("tournaments", tournaments);
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