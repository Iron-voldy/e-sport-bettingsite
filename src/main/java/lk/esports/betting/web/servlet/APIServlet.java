package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.entity.User;
import lk.esports.betting.entity.Match;
import lk.esports.betting.utils.DatabaseUtil;

import jakarta.ejb.EJB;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/api/*")
public class APIServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(APIServlet.class.getName());

    @EJB
    private UserService userService;

    @EJB
    private MatchService matchService;

    @EJB
    private BettingService bettingService;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleApiRoot(request, response);
            } else if (pathInfo.equals("/user/balance")) {
                handleGetUserBalance(request, response);
            } else if (pathInfo.equals("/matches/live")) {
                handleGetLiveMatches(request, response);
            } else if (pathInfo.equals("/matches/upcoming")) {
                handleGetUpcomingMatches(request, response);
            } else if (pathInfo.equals("/health")) {
                handleHealthCheck(request, response);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "API endpoint not found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in API servlet", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void handleApiRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "E-Sports Betting API");
        apiInfo.put("version", "1.0");
        apiInfo.put("status", "active");
        apiInfo.put("endpoints", Map.of(
                "user_balance", "/api/user/balance",
                "live_matches", "/api/matches/live",
                "upcoming_matches", "/api/matches/upcoming",
                "health_check", "/api/health"
        ));
        sendJsonResponse(response, apiInfo);
    }

    private void handleGetUserBalance(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to access your balance");
            return;
        }

        try {
            if (userService == null) {
                sendJsonError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "User service unavailable");
                return;
            }

            Long userId = (Long) session.getAttribute("userId");
            User user = userService.findUserById(userId);

            if (user != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("balance", user.getWalletBalance());
                result.put("userId", userId);
                sendJsonResponse(response, result);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user balance", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving balance");
        }
    }

    private void handleGetLiveMatches(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (matchService == null) {
                sendJsonError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Match service unavailable");
                return;
            }

            List<Match> liveMatches = matchService.getLiveMatches();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("matches", liveMatches != null ? liveMatches : List.of());
            result.put("count", liveMatches != null ? liveMatches.size() : 0);

            sendJsonResponse(response, result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting live matches", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving live matches");
        }
    }

    private void handleGetUpcomingMatches(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (matchService == null) {
                sendJsonError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Match service unavailable");
                return;
            }

            List<Match> upcomingMatches = matchService.getUpcomingMatches();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("matches", upcomingMatches != null ? upcomingMatches : List.of());
            result.put("count", upcomingMatches != null ? upcomingMatches.size() : 0);

            sendJsonResponse(response, result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting upcoming matches", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving upcoming matches");
        }
    }

    private void handleHealthCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", System.currentTimeMillis());

        Map<String, String> services = new HashMap<>();
        services.put("userService", userService != null ? "available" : "unavailable");
        services.put("matchService", matchService != null ? "available" : "unavailable");
        services.put("bettingService", bettingService != null ? "available" : "unavailable");

        // Test database connection
        try {
            boolean dbHealthy = DatabaseUtil.isDatabaseHealthy();
            services.put("database", dbHealthy ? "healthy" : "unhealthy");
        } catch (Exception e) {
            services.put("database", "error");
        }

        health.put("services", services);
        sendJsonResponse(response, health);
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }

    private void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("statusCode", statusCode);
        error.put("timestamp", System.currentTimeMillis());

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(error));
            out.flush();
        }
    }
}