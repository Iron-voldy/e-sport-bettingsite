package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.entity.Bet;
import lk.esports.betting.entity.User;
import lk.esports.betting.entity.Match;

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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/bets/*")
public class BetServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(BetServlet.class.getName());

    @EJB
    private BettingService bettingService;

    @EJB
    private UserService userService;

    @EJB
    private MatchService matchService;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetUserBets(request, response, userId);
            } else if (pathInfo.equals("/history")) {
                handleGetBettingHistory(request, response, userId);
            } else if (pathInfo.equals("/pending")) {
                handleGetPendingBets(request, response, userId);
            } else if (pathInfo.equals("/calculate")) {
                handleCalculatePotentialWinnings(request, response);
            } else if (pathInfo.startsWith("/details/")) {
                handleGetBetDetails(request, response, pathInfo, userId);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in BetServlet GET", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to place bets");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/place")) {
                handlePlaceBet(request, response, userId);
            } else if (pathInfo != null && pathInfo.startsWith("/cancel/")) {
                handleCancelBet(request, response, pathInfo, userId);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in BetServlet POST", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void handleGetUserBets(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        List<Bet> userBets = bettingService.getUserBets(userId);

        if ("json".equals(format)) {
            sendJsonResponse(response, userBets);
        } else {
            request.setAttribute("userBets", userBets);
            request.setAttribute("pageTitle", "My Bets");
            request.getRequestDispatcher("/my-bets.jsp").forward(request, response);
        }
    }

    private void handleGetBettingHistory(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        String status = request.getParameter("status");

        List<Bet> bets;
        if ("won".equals(status)) {
            bets = bettingService.getUserWinningBets(userId);
        } else if ("lost".equals(status)) {
            bets = bettingService.getUserLosingBets(userId);
        } else if ("pending".equals(status)) {
            bets = bettingService.getUserPendingBets(userId);
        } else {
            bets = bettingService.getUserBets(userId);
        }

        if ("json".equals(format)) {
            Map<String, Object> response_data = new HashMap<>();
            response_data.put("bets", bets);
            response_data.put("totalBets", bets.size());
            response_data.put("status", status != null ? status : "all");
            sendJsonResponse(response, response_data);
        } else {
            request.setAttribute("bets", bets);
            request.setAttribute("pageTitle", "Betting History");
            request.setAttribute("filterStatus", status);
            request.getRequestDispatcher("/betting-history.jsp").forward(request, response);
        }
    }

    private void handleGetPendingBets(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws ServletException, IOException {

        List<Bet> pendingBets = bettingService.getUserPendingBets(userId);

        String format = request.getParameter("format");
        if ("json".equals(format)) {
            sendJsonResponse(response, pendingBets);
        } else {
            request.setAttribute("pendingBets", pendingBets);
            request.setAttribute("pageTitle", "Pending Bets");
            request.getRequestDispatcher("/pending-bets.jsp").forward(request, response);
        }
    }

    private void handleCalculatePotentialWinnings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String matchIdStr = request.getParameter("matchId");
            String teamIdStr = request.getParameter("teamId");
            String amountStr = request.getParameter("amount");

            if (matchIdStr == null || teamIdStr == null || amountStr == null) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                return;
            }

            Long matchId = Long.parseLong(matchIdStr);
            Long teamId = Long.parseLong(teamIdStr);
            BigDecimal amount = new BigDecimal(amountStr);

            BigDecimal potentialWinnings = bettingService.calculatePotentialWinnings(matchId, teamId, amount);

            Map<String, Object> result = new HashMap<>();
            result.put("potentialWinnings", potentialWinnings);
            result.put("betAmount", amount);
            result.put("profit", potentialWinnings.subtract(amount));

            sendJsonResponse(response, result);

        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating potential winnings", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error calculating winnings");
        }
    }

    private void handleGetBetDetails(HttpServletRequest request, HttpServletResponse response, String pathInfo, Long userId)
            throws ServletException, IOException {

        try {
            String betIdStr = pathInfo.substring("/details/".length());
            Long betId = Long.parseLong(betIdStr);

            Bet bet = bettingService.findBetById(betId);
            if (bet == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Bet not found");
                return;
            }

            // Check if the bet belongs to the current user
            if (!bet.getUser().getId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            String format = request.getParameter("format");
            if ("json".equals(format)) {
                sendJsonResponse(response, bet);
            } else {
                request.setAttribute("bet", bet);
                request.setAttribute("pageTitle", "Bet Details");
                request.getRequestDispatcher("/bet-details.jsp").forward(request, response);
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid bet ID");
        }
    }

    private void handlePlaceBet(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws ServletException, IOException {

        try {
            String matchIdStr = request.getParameter("matchId");
            String teamIdStr = request.getParameter("teamId");
            String amountStr = request.getParameter("amount");

            // Validate input parameters
            if (matchIdStr == null || teamIdStr == null || amountStr == null) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                return;
            }

            Long matchId = Long.parseLong(matchIdStr);
            Long teamId = Long.parseLong(teamIdStr);
            BigDecimal amount = new BigDecimal(amountStr);

            // Validate bet amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Bet amount must be positive");
                return;
            }

            // Check minimum and maximum bet limits
            if (!bettingService.isValidBetAmount(amount)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        String.format("Bet amount must be between $%.2f and $%.2f",
                                bettingService.getMinimumBetAmount(), bettingService.getMaximumBetAmount()));
                return;
            }

            // Validate bet
            if (!bettingService.validateBet(userId, matchId, teamId, amount)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bet");
                return;
            }

            // Check if user can place bet
            if (!bettingService.canPlaceBet(userId, matchId, amount)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Cannot place bet. Check your balance and bet limits.");
                return;
            }

            // Place the bet
            Bet bet = bettingService.placeBet(userId, matchId, teamId, amount);

            if (bet != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Bet placed successfully");
                result.put("betId", bet.getId());
                result.put("potentialWinnings", bet.getPotentialWinnings());
                result.put("odds", bet.getOddsAtBet());

                // Update user balance in session
                User user = userService.findUserById(userId);
                if (user != null) {
                    HttpSession session = request.getSession();
                    session.setAttribute("userBalance", user.getWalletBalance());
                }

                sendJsonResponse(response, result);
                logger.info("Bet placed successfully: User " + userId + ", Match " + matchId + ", Amount $" + amount);

            } else {
                sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to place bet");
            }

        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
        } catch (IllegalArgumentException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error placing bet", e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error placing bet", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private void handleCancelBet(HttpServletRequest request, HttpServletResponse response, String pathInfo, Long userId)
            throws ServletException, IOException {

        try {
            String betIdStr = pathInfo.substring("/cancel/".length());
            Long betId = Long.parseLong(betIdStr);

            Bet bet = bettingService.findBetById(betId);
            if (bet == null) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Bet not found");
                return;
            }

            // Check if the bet belongs to the current user
            if (!bet.getUser().getId().equals(userId)) {
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            // Check if bet can be cancelled
            if (!bet.canBeCancelled()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Bet cannot be cancelled");
                return;
            }

            // Cancel the bet
            bettingService.cancelBet(betId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Bet cancelled successfully");
            result.put("refundAmount", bet.getBetAmount());

            // Update user balance in session
            User user = userService.findUserById(userId);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("userBalance", user.getWalletBalance());
            }

            sendJsonResponse(response, result);
            logger.info("Bet cancelled: " + betId + " by user " + userId);

        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bet ID");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error cancelling bet", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error cancelling bet");
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

    private void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("statusCode", statusCode);

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(error));
            out.flush();
        }
    }
}