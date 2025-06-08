package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.ejb.local.MatchService;
import lk.esports.betting.ejb.local.BettingService;
import lk.esports.betting.entity.User;
import lk.esports.betting.entity.Match;
import lk.esports.betting.entity.Bet;
import lk.esports.betting.entity.Transaction;
import lk.esports.betting.utils.EJBServiceLocator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(DashboardServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            // Store the requested URL for redirect after login
            session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/dashboard");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");

        try {
            // Get services using service locator
            UserService userService = EJBServiceLocator.getUserService();

            if (userService == null) {
                logger.severe("UserService is null - service locator failed");
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // Get user information
            User user = userService.findUserById(userId);
            if (user == null || !user.getIsActive()) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // Update user balance in session (in case it changed)
            session.setAttribute("userBalance", user.getWalletBalance());
            session.setAttribute("user", user);

            // Get dashboard data
            loadDashboardData(request, userId);

            // Check for success/error messages
            String successMessage = (String) session.getAttribute("successMessage");
            String errorMessage = (String) session.getAttribute("errorMessage");

            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }

            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }

            // Forward to dashboard page
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading dashboard for user: " + userId, e);
            request.setAttribute("errorMessage", "Error loading dashboard. Please try again.");
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        Long userId = (Long) session.getAttribute("userId");

        try {
            switch (action) {
                case "addFunds":
                    handleAddFunds(request, response, session, userId);
                    break;
                case "withdrawFunds":
                    handleWithdrawFunds(request, response, session, userId);
                    break;
                case "updateProfile":
                    handleUpdateProfile(request, response, session, userId);
                    break;
                case "logout":
                    handleLogout(request, response, session);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/dashboard");
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling dashboard action: " + action, e);
            session.setAttribute("errorMessage", "An error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    private void loadDashboardData(HttpServletRequest request, Long userId) {
        try {
            // Get services
            MatchService matchService = EJBServiceLocator.getMatchService();
            BettingService bettingService = EJBServiceLocator.getBettingService();
            UserService userService = EJBServiceLocator.getUserService();

            // Get upcoming matches for betting
            List<Match> upcomingMatches = List.of();
            List<Match> liveMatches = List.of();

            if (matchService != null) {
                try {
                    upcomingMatches = matchService.getUpcomingMatches();
                    liveMatches = matchService.getLiveMatches();

                    // Limit to first 6 matches for dashboard display
                    if (upcomingMatches != null && upcomingMatches.size() > 6) {
                        upcomingMatches = upcomingMatches.subList(0, 6);
                    }
                    if (liveMatches != null && liveMatches.size() > 3) {
                        liveMatches = liveMatches.subList(0, 3);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading matches", e);
                    upcomingMatches = List.of();
                    liveMatches = List.of();
                }
            }

            // Get user's recent bets
            List<Bet> recentBets = List.of();
            List<Bet> pendingBets = List.of();

            if (bettingService != null) {
                try {
                    recentBets = bettingService.getUserBets(userId);
                    if (recentBets != null && recentBets.size() > 5) {
                        recentBets = recentBets.subList(0, 5);
                    }
                    pendingBets = bettingService.getUserPendingBets(userId);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading user bets", e);
                    recentBets = List.of();
                    pendingBets = List.of();
                }
            }

            // Get user's recent transactions
            List<Transaction> recentTransactions = List.of();
            if (userService != null) {
                try {
                    recentTransactions = userService.getUserTransactions(userId);
                    if (recentTransactions != null && recentTransactions.size() > 5) {
                        recentTransactions = recentTransactions.subList(0, 5);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading user transactions", e);
                    recentTransactions = List.of();
                }
            }

            // Get user statistics
            Map<String, Object> userStats = getUserStatistics(userId);

            // Set attributes for JSP (always set, even if empty)
            request.setAttribute("upcomingMatches", upcomingMatches != null ? upcomingMatches : List.of());
            request.setAttribute("liveMatches", liveMatches != null ? liveMatches : List.of());
            request.setAttribute("recentBets", recentBets != null ? recentBets : List.of());
            request.setAttribute("pendingBets", pendingBets != null ? pendingBets : List.of());
            request.setAttribute("recentTransactions", recentTransactions != null ? recentTransactions : List.of());
            request.setAttribute("userStats", userStats);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading dashboard data for user: " + userId, e);
            // Set empty lists to avoid JSP errors
            request.setAttribute("upcomingMatches", List.of());
            request.setAttribute("liveMatches", List.of());
            request.setAttribute("recentBets", List.of());
            request.setAttribute("pendingBets", List.of());
            request.setAttribute("recentTransactions", List.of());
            request.setAttribute("userStats", Map.of());
        }
    }

    private Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> defaultStats = Map.of(
                "totalBetAmount", BigDecimal.ZERO,
                "totalWinnings", BigDecimal.ZERO,
                "totalBets", 0,
                "wonBets", 0,
                "winRate", 0.0,
                "currentBalance", BigDecimal.ZERO,
                "netProfit", BigDecimal.ZERO
        );

        try {
            BettingService bettingService = EJBServiceLocator.getBettingService();
            UserService userService = EJBServiceLocator.getUserService();

            if (bettingService == null || userService == null) {
                return defaultStats;
            }

            BigDecimal totalBetAmount = BigDecimal.ZERO;
            BigDecimal totalWinnings = BigDecimal.ZERO;
            int totalBets = 0;
            int wonBets = 0;
            double winRate = 0.0;
            BigDecimal currentBalance = BigDecimal.ZERO;

            try {
                totalBetAmount = bettingService.getUserTotalBetAmount(userId);
                if (totalBetAmount == null) totalBetAmount = BigDecimal.ZERO;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting total bet amount", e);
            }

            try {
                totalWinnings = bettingService.getUserTotalWinnings(userId);
                if (totalWinnings == null) totalWinnings = BigDecimal.ZERO;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting total winnings", e);
            }

            try {
                totalBets = userService.getTotalBetsPlaced(userId);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting total bets", e);
            }

            try {
                wonBets = userService.getWonBetsCount(userId);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting won bets count", e);
            }

            try {
                winRate = bettingService.getUserWinRate(userId);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting win rate", e);
            }

            try {
                currentBalance = userService.getUserBalance(userId);
                if (currentBalance == null) currentBalance = BigDecimal.ZERO;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting current balance", e);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBetAmount", totalBetAmount);
            stats.put("totalWinnings", totalWinnings);
            stats.put("totalBets", totalBets);
            stats.put("wonBets", wonBets);
            stats.put("winRate", winRate);
            stats.put("currentBalance", currentBalance);
            stats.put("netProfit", totalWinnings.subtract(totalBetAmount));

            return stats;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user statistics: " + userId, e);
            return defaultStats;
        }
    }

    private void handleAddFunds(HttpServletRequest request, HttpServletResponse response,
                                HttpSession session, Long userId) throws IOException {

        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            if (amountStr == null || amountStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "Amount is required");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                session.setAttribute("errorMessage", "Amount must be positive");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            if (amount.compareTo(new BigDecimal("10000")) > 0) {
                session.setAttribute("errorMessage", "Maximum deposit amount is $10,000");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            // Add funds to user account
            UserService userService = EJBServiceLocator.getUserService();
            if (userService != null) {
                String finalDescription = description != null && !description.trim().isEmpty()
                        ? description.trim() : "Account deposit";

                userService.addFunds(userId, amount, finalDescription);

                // Update balance in session
                BigDecimal newBalance = userService.getUserBalance(userId);
                session.setAttribute("userBalance", newBalance);

                session.setAttribute("successMessage",
                        String.format("Successfully added $%.2f to your account", amount));

                logger.info("Funds added: User " + userId + ", Amount $" + amount);
            } else {
                session.setAttribute("errorMessage", "Service unavailable. Please try again.");
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Invalid amount format");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding funds for user: " + userId, e);
            session.setAttribute("errorMessage", "Error adding funds. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void handleWithdrawFunds(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, Long userId) throws IOException {

        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            if (amountStr == null || amountStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "Amount is required");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                session.setAttribute("errorMessage", "Amount must be positive");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            UserService userService = EJBServiceLocator.getUserService();
            if (userService != null) {
                // Check if user has sufficient balance
                if (!userService.canPlaceBet(userId, amount)) {
                    session.setAttribute("errorMessage", "Insufficient balance for withdrawal");
                    response.sendRedirect(request.getContextPath() + "/dashboard");
                    return;
                }

                // Withdraw funds from user account
                String finalDescription = description != null && !description.trim().isEmpty()
                        ? description.trim() : "Account withdrawal";

                boolean success = userService.withdrawFunds(userId, amount, finalDescription);

                if (success) {
                    // Update balance in session
                    BigDecimal newBalance = userService.getUserBalance(userId);
                    session.setAttribute("userBalance", newBalance);

                    session.setAttribute("successMessage",
                            String.format("Successfully withdrew $%.2f from your account", amount));

                    logger.info("Funds withdrawn: User " + userId + ", Amount $" + amount);
                } else {
                    session.setAttribute("errorMessage", "Withdrawal failed. Please try again.");
                }
            } else {
                session.setAttribute("errorMessage", "Service unavailable. Please try again.");
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Invalid amount format");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error withdrawing funds for user: " + userId, e);
            session.setAttribute("errorMessage", "Error processing withdrawal. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, Long userId) throws IOException {

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");

        try {
            if (fullName == null || fullName.trim().isEmpty()) {
                session.setAttribute("errorMessage", "Full name is required");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            UserService userService = EJBServiceLocator.getUserService();
            if (userService != null) {
                // Update user profile
                userService.updateProfile(userId, fullName.trim(),
                        phone != null ? phone.trim() : null);

                // Update user object in session
                User updatedUser = userService.findUserById(userId);
                session.setAttribute("user", updatedUser);

                session.setAttribute("successMessage", "Profile updated successfully");
                logger.info("Profile updated for user: " + userId);
            } else {
                session.setAttribute("errorMessage", "Service unavailable. Please try again.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating profile for user: " + userId, e);
            session.setAttribute("errorMessage", "Error updating profile. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) throws IOException {

        Long userId = (Long) session.getAttribute("userId");

        // Invalidate session
        session.invalidate();

        logger.info("User logged out: " + userId);

        // Redirect to home page with logout message
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("successMessage", "You have been successfully logged out");

        response.sendRedirect(request.getContextPath() + "/");
    }
}