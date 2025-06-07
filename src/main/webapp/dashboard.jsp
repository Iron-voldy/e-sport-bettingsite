<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - ML Betting</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar">
        <div class="container">
            <a href="${pageContext.request.contextPath}/" class="navbar-brand">
                <img src="https://via.placeholder.com/40x40/6C5CE7/FFFFFF?text=ML" alt="ML Betting">
                ML Betting
            </a>

            <ul class="navbar-nav">
                <li><a href="${pageContext.request.contextPath}/" class="nav-link">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/matches/upcoming" class="nav-link">Matches</a></li>
                <li><a href="${pageContext.request.contextPath}/dashboard" class="nav-link active">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a></li>
                <li><a href="${pageContext.request.contextPath}/dashboard?action=logout" class="nav-link">Logout</a></li>
            </ul>
        </div>
    </nav>

    <!-- Dashboard Header -->
    <section class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Welcome back, ${sessionScope.user.username}!</h1>
                <p class="text-secondary">Ready to bet on some exciting Mobile Legends matches?</p>
            </div>
            <div class="d-flex" style="gap: 1rem;">
                <button class="btn btn-success" data-modal-target="addFundsModal">Add Funds</button>
                <button class="btn btn-outline" data-modal-target="withdrawModal">Withdraw</button>
            </div>
        </div>

        <!-- Display Messages -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">
                <span>${successMessage}</span>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <span>${errorMessage}</span>
            </div>
        </c:if>
    </section>

    <!-- User Stats -->
    <section class="container">
        <div class="stats-grid">
            <div class="stat-card">
                <span class="stat-value user-balance">
                    $<fmt:formatNumber value="${sessionScope.user.walletBalance}" pattern="#,##0.00"/>
                </span>
                <span class="stat-label">Current Balance</span>
            </div>
            <div class="stat-card">
                <span class="stat-value text-primary">
                    $<fmt:formatNumber value="${userStats.totalBetAmount}" pattern="#,##0.00"/>
                </span>
                <span class="stat-label">Total Bets</span>
            </div>
            <div class="stat-card">
                <span class="stat-value text-success">
                    $<fmt:formatNumber value="${userStats.totalWinnings}" pattern="#,##0.00"/>
                </span>
                <span class="stat-label">Total Winnings</span>
            </div>
            <div class="stat-card">
                <span class="stat-value text-warning">
                    <fmt:formatNumber value="${userStats.winRate}" pattern="#0.0"/>%
                </span>
                <span class="stat-label">Win Rate</span>
            </div>
        </div>
    </section>

    <!-- Main Dashboard Content -->
    <section class="container mt-5">
        <div class="d-flex" style="gap: 2rem;">
            <!-- Left Column -->
            <div style="flex: 2;">
                <!-- Live Matches -->
                <c:if test="${not empty liveMatches}">
                    <div class="card mb-4">
                        <div class="card-header">
                            <h3 style="margin: 0; display: flex; align-items: center;">
                                🔴 Live Matches
                                <span class="badge badge-danger" style="margin-left: 1rem;">${liveMatches.size()}</span>
                            </h3>
                        </div>
                        <div class="card-body">
                            <c:forEach items="${liveMatches}" var="match">
                                <div class="match-card" data-match-id="${match.id}">
                                    <div class="match-header">
                                        <span class="match-status status-live">LIVE</span>
                                        <span class="match-date">
                                            <fmt:formatDate value="${match.matchDate}" pattern="HH:mm" />
                                        </span>
                                    </div>

                                    <div class="teams-container">
                                        <div class="team">
                                            <img src="https://via.placeholder.com/50x50/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                                 alt="${match.team1.teamName}" class="team-logo" style="width: 50px; height: 50px;">
                                            <div class="team-name">${match.team1.teamName}</div>
                                            <div class="team-odds">${match.team1Odds}</div>
                                        </div>

                                        <div class="vs-divider">VS</div>

                                        <div class="team">
                                            <img src="https://via.placeholder.com/50x50/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                                 alt="${match.team2.teamName}" class="team-logo" style="width: 50px; height: 50px;">
                                            <div class="team-name">${match.team2.teamName}</div>
                                            <div class="team-odds">${match.team2Odds}</div>
                                        </div>
                                    </div>

                                    <div class="match-info">
                                        <span class="tournament-name">${match.tournament.tournamentName}</span>
                                        <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-danger btn-sm">
                                            Watch Live
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <!-- Upcoming Matches -->
                <div class="card">
                    <div class="card-header">
                        <h3 style="margin: 0;">⏰ Upcoming Matches</h3>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty upcomingMatches}">
                                <c:forEach items="${upcomingMatches}" var="match">
                                    <div class="match-card" data-match-id="${match.id}">
                                        <div class="match-header">
                                            <span class="match-status status-upcoming">UPCOMING</span>
                                            <span class="match-date countdown" data-end-time="${match.matchDate}">
                                                <fmt:formatDate value="${match.matchDate}" pattern="MMM dd, HH:mm" />
                                            </span>
                                        </div>

                                        <div class="teams-container">
                                            <div class="team">
                                                <img src="https://via.placeholder.com/50x50/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                                     alt="${match.team1.teamName}" class="team-logo" style="width: 50px; height: 50px;">
                                                <div class="team-name">${match.team1.teamName}</div>
                                                <button class="btn btn-sm btn-outline team-select-btn"
                                                        data-match-id="${match.id}"
                                                        data-team-id="${match.team1.id}"
                                                        data-team-name="${match.team1.teamName}"
                                                        data-odds="${match.team1Odds}">
                                                    ${match.team1Odds}
                                                </button>
                                            </div>

                                            <div class="vs-divider">VS</div>

                                            <div class="team">
                                                <img src="https://via.placeholder.com/50x50/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                                     alt="${match.team2.teamName}" class="team-logo" style="width: 50px; height: 50px;">
                                                <div class="team-name">${match.team2.teamName}</div>
                                                <button class="btn btn-sm btn-outline team-select-btn"
                                                        data-match-id="${match.id}"
                                                        data-team-id="${match.team2.id}"
                                                        data-team-name="${match.team2.teamName}"
                                                        data-odds="${match.team2Odds}">
                                                    ${match.team2Odds}
                                                </button>
                                            </div>
                                        </div>

                                        <!-- Betting Panel (Initially Hidden) -->
                                        <div class="betting-panel" style="display: none;">
                                            <h5>Place Your Bet</h5>
                                            <p>Selected: <span class="selected-team-display"></span> @ <span class="selected-odds-display"></span></p>

                                            <div class="betting-form">
                                                <div class="form-group">
                                                    <label class="form-label">Bet Amount</label>
                                                    <input type="number" class="form-control bet-amount-input"
                                                           placeholder="Enter amount" min="1" max="10000" step="0.01">
                                                </div>

                                                <div class="d-flex" style="gap: 0.5rem;">
                                                    <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="10">$10</button>
                                                    <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="25">$25</button>
                                                    <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="50">$50</button>
                                                    <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="100">$100</button>
                                                </div>

                                                <button class="btn btn-primary place-bet-btn">Place Bet</button>
                                            </div>

                                            <div class="potential-winnings">
                                                <div>Potential Winnings</div>
                                                <div class="potential-winnings-amount">$0.00</div>
                                                <div class="potential-profit-amount">Profit: $0.00</div>
                                            </div>
                                        </div>

                                        <div class="match-info">
                                            <span class="tournament-name">${match.tournament.tournamentName}</span>
                                            <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-primary btn-sm">
                                                View Details
                                            </a>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center" style="padding: 2rem;">
                                    <h4>No upcoming matches</h4>
                                    <p class="text-secondary">Check back later for new betting opportunities!</p>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="text-center mt-3">
                            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-secondary">
                                View All Matches
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right Column -->
            <div style="flex: 1;">
                <!-- Quick Actions -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h4 style="margin: 0;">Quick Actions</h4>
                    </div>
                    <div class="card-body">
                        <div style="display: flex; flex-direction: column; gap: 1rem;">
                            <button class="btn btn-success w-100" data-modal-target="addFundsModal">
                                💰 Add Funds
                            </button>
                            <button class="btn btn-outline w-100" data-modal-target="withdrawModal">
                                💸 Withdraw
                            </button>
                            <a href="${pageContext.request.contextPath}/bets/history" class="btn btn-outline w-100">
                                📊 Betting History
                            </a>
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline w-100">
                                ⚙️ Account Settings
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Recent Bets -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h4 style="margin: 0;">Recent Bets</h4>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty recentBets}">
                                <c:forEach items="${recentBets}" var="bet">
                                    <div style="border-bottom: 1px solid var(--border-color); padding: 1rem 0;">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <div>
                                                <div style="font-weight: 600;">${bet.selectedTeam.teamName}</div>
                                                <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                    ${bet.match.team1.teamName} vs ${bet.match.team2.teamName}
                                                </div>
                                            </div>
                                            <div style="text-align: right;">
                                                <div style="font-weight: 600;">$${bet.betAmount}</div>
                                                <div class="badge badge-${bet.status == 'WON' ? 'success' : bet.status == 'LOST' ? 'danger' : 'warning'}">
                                                    ${bet.status}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center" style="padding: 1rem;">
                                    <p style="margin: 0; color: var(--text-secondary);">No recent bets</p>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="text-center mt-3">
                            <a href="${pageContext.request.contextPath}/bets/history" class="btn btn-sm btn-outline">
                                View All Bets
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Pending Bets -->
                <c:if test="${not empty pendingBets}">
                    <div class="card">
                        <div class="card-header">
                            <h4 style="margin: 0;">
                                Pending Bets
                                <span class="badge badge-warning">${pendingBets.size()}</span>
                            </h4>
                        </div>
                        <div class="card-body">
                            <c:forEach items="${pendingBets}" var="bet">
                                <div style="border-bottom: 1px solid var(--border-color); padding: 1rem 0;">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <div style="font-weight: 600;">${bet.selectedTeam.teamName}</div>
                                            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                Potential: $${bet.potentialWinnings}
                                            </div>
                                        </div>
                                        <div style="text-align: right;">
                                            <div style="font-weight: 600; color: var(--warning-color);">$${bet.betAmount}</div>
                                            <c:if test="${bet.canBeCancelled}">
                                                <button class="btn btn-sm btn-danger" onclick="cancelBet(${bet.id})">
                                                    Cancel
                                                </button>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </section>

    <!-- Add Funds Modal -->
    <div id="addFundsModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; align-items: center; justify-content: center;">
        <div class="card" style="width: 100%; max-width: 400px; margin: 2rem;">
            <div class="card-header">
                <h4 style="margin: 0;">Add Funds</h4>
            </div>
            <div class="card-body">
                <form id="addFundsForm" action="${pageContext.request.contextPath}/dashboard" method="POST">
                    <input type="hidden" name="action" value="addFunds">

                    <div class="form-group">
                        <label for="amount" class="form-label">Amount ($)</label>
                        <input type="number" id="amount" name="amount" class="form-control"
                               placeholder="Enter amount" min="10" max="10000" step="0.01" required>
                        <div class="form-text">Minimum: $10, Maximum: $10,000</div>
                    </div>

                    <div class="form-group">
                        <label for="description" class="form-label">Description (Optional)</label>
                        <input type="text" id="description" name="description" class="form-control"
                               placeholder="e.g., Tournament betting fund">
                    </div>

                    <div class="d-flex" style="gap: 1rem;">
                        <button type="submit" class="btn btn-success">Add Funds</button>
                        <button type="button" class="btn btn-outline modal-close">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Withdraw Modal -->
    <div id="withdrawModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; align-items: center; justify-content: center;">
        <div class="card" style="width: 100%; max-width: 400px; margin: 2rem;">
            <div class="card-header">
                <h4 style="margin: 0;">Withdraw Funds</h4>
            </div>
            <div class="card-body">
                <form id="withdrawForm" action="${pageContext.request.contextPath}/dashboard" method="POST">
                    <input type="hidden" name="action" value="withdrawFunds">

                    <div class="alert alert-info">
                        <span>Available Balance: $<fmt:formatNumber value="${sessionScope.user.walletBalance}" pattern="#,##0.00"/></span>
                    </div>

                    <div class="form-group">
                        <label for="withdrawAmount" class="form-label">Amount ($)</label>
                        <input type="number" id="withdrawAmount" name="amount" class="form-control"
                               placeholder="Enter amount" min="1" max="${sessionScope.user.walletBalance}" step="0.01" required>
                    </div>

                    <div class="form-group">
                        <label for="withdrawDescription" class="form-label">Description (Optional)</label>
                        <input type="text" id="withdrawDescription" name="description" class="form-control"
                               placeholder="e.g., Withdraw winnings">
                    </div>

                    <div class="d-flex" style="gap: 1rem;">
                        <button type="submit" class="btn btn-warning">Withdraw</button>
                        <button type="button" class="btn btn-outline modal-close">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer">
        <div class="container">
            <div class="footer-bottom">
                <p>&copy; 2024 ML Betting Platform. All rights reserved. |
                   <span class="text-warning">Please bet responsibly. 18+ only.</span>
                </p>
            </div>
        </div>
    </footer>

    <!-- Alert Container -->
    <div class="alert-container"></div>

    <!-- Scripts -->
    <script src="${pageContext.request.contextPath}/js/script.js"></script>

    <script>
        // Set user balance for JavaScript
        userBalance = ${sessionScope.user.walletBalance};

        // Cancel bet function
        function cancelBet(betId) {
            if (confirm('Are you sure you want to cancel this bet?')) {
                fetch('${pageContext.request.contextPath}/bets/cancel/' + betId, {
                    method: 'POST'
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showAlert('Bet cancelled successfully! Refund: ' + data.refundAmount, 'success');
                        location.reload();
                    } else {
                        showAlert(data.error || 'Failed to cancel bet', 'danger');
                    }
                })
                .catch(error => {
                    showAlert('Error cancelling bet', 'danger');
                });
            }
        }
    </script>
</body>
</html>