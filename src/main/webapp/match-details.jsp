<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Match Details - ${match.team1.teamName} vs ${match.team2.teamName} - ML Betting</title>
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
                <c:if test="${sessionScope.user != null}">
                    <li><a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a></li>
                    <li class="nav-item" style="color: var(--text-secondary); padding: 0.5rem 1rem;">
                        Balance: $<span class="user-balance"><fmt:formatNumber value="${sessionScope.user.walletBalance}" pattern="#,##0.00"/></span>
                    </li>
                    <li><a href="${pageContext.request.contextPath}/dashboard?action=logout" class="nav-link">Logout</a></li>
                </c:if>
                <c:if test="${sessionScope.user == null}">
                    <li><a href="${pageContext.request.contextPath}/login" class="nav-link">Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Sign Up</a></li>
                </c:if>
            </ul>
        </div>
    </nav>

    <!-- Match Header -->
    <section style="background: linear-gradient(135deg, var(--dark-surface), var(--dark-surface-light)); padding: 2rem 0; border-bottom: 1px solid var(--border-color);">
        <div class="container">
            <div class="text-center">
                <div class="d-flex justify-content-center align-items-center" style="gap: 2rem; margin-bottom: 1rem;">
                    <span class="match-status ${match.status == 'LIVE' ? 'status-live' : match.status == 'COMPLETED' ? 'status-completed' : 'status-upcoming'}">
                        ${match.status}
                    </span>
                    <span class="countdown" data-end-time="${match.matchDate}">
                        <fmt:formatDate value="${match.matchDateAsDate}" pattern="MMM dd, yyyy HH:mm" />
                    </span>
                </div>

                <h1 style="margin-bottom: 0.5rem;">${match.team1.teamName} vs ${match.team2.teamName}</h1>
                <p class="text-secondary">
                    <c:choose>
                        <c:when test="${not empty match.tournament and not empty match.tournament.tournamentName}">
                            ${match.tournament.tournamentName}
                        </c:when>
                        <c:otherwise>
                            Tournament TBD
                        </c:otherwise>
                    </c:choose>
                    • ${match.matchType}
                </p>
            </div>
        </div>
    </section>

    <!-- Main Content -->
    <section class="container mt-4">
        <div class="d-flex" style="gap: 2rem;">
            <!-- Left Column - Match Info -->
            <div style="flex: 2;">
                <!-- Teams Comparison -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h3 style="margin: 0;">Team Matchup</h3>
                    </div>
                    <div class="card-body">
                        <div class="teams-container" style="align-items: stretch;">
                            <!-- Team 1 -->
                            <div class="team" style="flex: 1; padding: 1.5rem; background: rgba(255, 107, 107, 0.1); border-radius: 12px;">
                                <img src="https://via.placeholder.com/100x100/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                     alt="${match.team1.teamName}" style="width: 100px; height: 100px; border-radius: 50%; margin-bottom: 1rem;">
                                <h4>${match.team1.teamName}</h4>
                                <p class="text-secondary">${match.team1.country}</p>

                                <div style="margin: 1rem 0;">
                                    <div class="d-flex justify-content-between">
                                        <span>Win Rate:</span>
                                        <strong><fmt:formatNumber value="${match.team1.winRate}" pattern="#0.0"/>%</strong>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Total Matches:</span>
                                        <strong>${match.team1.totalMatches}</strong>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Current Odds:</span>
                                        <strong>${match.team1Odds}</strong>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${match.status == 'COMPLETED'}">
                                        <div class="team-score ${match.winnerTeam.id == match.team1.id ? 'text-success' : 'text-secondary'}"
                                             style="font-size: 2rem; font-weight: 700; text-align: center;">
                                            ${match.team1Score}
                                            <c:if test="${match.winnerTeam.id == match.team1.id}">
                                                <div style="font-size: 1rem; color: var(--success-color);">WINNER</div>
                                            </c:if>
                                        </div>
                                    </c:when>
                                    <c:when test="${sessionScope.user != null && match.status == 'SCHEDULED'}">
                                        <button class="btn btn-primary w-100 team-select-btn"
                                                data-match-id="${match.id}"
                                                data-team-id="${match.team1.id}"
                                                data-team-name="${match.team1.teamName}"
                                                data-odds="${match.team1Odds}">
                                            Bet on ${match.team1.teamName} @ ${match.team1Odds}
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-center">
                                            <div style="font-size: 1.5rem; font-weight: 600;">Odds: ${match.team1Odds}</div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <!-- VS Divider -->
                            <div class="vs-divider" style="display: flex; align-items: center; padding: 0 2rem;">
                                <div style="text-align: center;">
                                    <c:choose>
                                        <c:when test="${match.status == 'COMPLETED'}">
                                            <div style="font-size: 2rem; font-weight: 700; color: var(--success-color);">FINAL</div>
                                            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                Match completed
                                            </div>
                                        </c:when>
                                        <c:when test="${match.status == 'LIVE'}">
                                            <div style="font-size: 2rem; font-weight: 700; color: var(--danger-color);">LIVE</div>
                                            <div style="font-size: 0.875rem; color: var(--danger-color);">
                                                Match in progress
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="font-size: 3rem; font-weight: 700; color: var(--text-muted);">VS</div>
                                            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                <span class="countdown" data-end-time="${match.matchDate}">
                                                    <fmt:formatDate value="${match.matchDateAsDate}" pattern="HH:mm" />
                                                </span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- Team 2 -->
                            <div class="team" style="flex: 1; padding: 1.5rem; background: rgba(0, 210, 211, 0.1); border-radius: 12px;">
                                <img src="https://via.placeholder.com/100x100/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                     alt="${match.team2.teamName}" style="width: 100px; height: 100px; border-radius: 50%; margin-bottom: 1rem;">
                                <h4>${match.team2.teamName}</h4>
                                <p class="text-secondary">${match.team2.country}</p>

                                <div style="margin: 1rem 0;">
                                    <div class="d-flex justify-content-between">
                                        <span>Win Rate:</span>
                                        <strong><fmt:formatNumber value="${match.team2.winRate}" pattern="#0.0"/>%</strong>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Total Matches:</span>
                                        <strong>${match.team2.totalMatches}</strong>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Current Odds:</span>
                                        <strong>${match.team2Odds}</strong>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${match.status == 'COMPLETED'}">
                                        <div class="team-score ${match.winnerTeam.id == match.team2.id ? 'text-success' : 'text-secondary'}"
                                             style="font-size: 2rem; font-weight: 700; text-align: center;">
                                            ${match.team2Score}
                                            <c:if test="${match.winnerTeam.id == match.team2.id}">
                                                <div style="font-size: 1rem; color: var(--success-color);">WINNER</div>
                                            </c:if>
                                        </div>
                                    </c:when>
                                    <c:when test="${sessionScope.user != null && match.status == 'SCHEDULED'}">
                                        <button class="btn btn-secondary w-100 team-select-btn"
                                                data-match-id="${match.id}"
                                                data-team-id="${match.team2.id}"
                                                data-team-name="${match.team2.teamName}"
                                                data-odds="${match.team2Odds}">
                                            Bet on ${match.team2.teamName} @ ${match.team2Odds}
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-center">
                                            <div style="font-size: 1.5rem; font-weight: 600;">Odds: ${match.team2Odds}</div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Match Statistics -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h3 style="margin: 0;">Match Statistics</h3>
                    </div>
                    <div class="card-body">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <span class="stat-value">${bettingStats.totalBets != null ? bettingStats.totalBets : 0}</span>
                                <span class="stat-label">Total Bets</span>
                            </div>
                            <div class="stat-card">
                                <span class="stat-value">$<fmt:formatNumber value="${bettingStats.totalAmount != null ? bettingStats.totalAmount : 0}" pattern="#,##0"/></span>
                                <span class="stat-label">Total Pool</span>
                            </div>
                            <div class="stat-card">
                                <span class="stat-value">${bettingStats.team1Bets != null ? bettingStats.team1Bets : 0}</span>
                                <span class="stat-label">${match.team1.teamName} Bets</span>
                            </div>
                            <div class="stat-card">
                                <span class="stat-value">${bettingStats.team2Bets != null ? bettingStats.team2Bets : 0}</span>
                                <span class="stat-label">${match.team2.teamName} Bets</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Match Information -->
                <div class="card">
                    <div class="card-header">
                        <h3 style="margin: 0;">Match Information</h3>
                    </div>
                    <div class="card-body">
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                            <div>
                                <strong>Tournament:</strong>
                                <div>
                                    <c:choose>
                                        <c:when test="${not empty match.tournament and not empty match.tournament.tournamentName}">
                                            ${match.tournament.tournamentName}
                                        </c:when>
                                        <c:otherwise>
                                            Tournament TBD
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div>
                                <strong>Format:</strong>
                                <div>${match.matchType}</div>
                            </div>
                            <div>
                                <strong>Date & Time:</strong>
                                <div><fmt:formatDate value="${match.matchDateAsDate}" pattern="MMM dd, yyyy HH:mm" /></div>
                            </div>
                            <div>
                                <strong>Status:</strong>
                                <div>
                                    <span class="badge badge-${match.status == 'LIVE' ? 'danger' : match.status == 'COMPLETED' ? 'success' : 'warning'}">
                                        ${match.status}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right Column - Betting Panel -->
            <div style="flex: 1;">
                <c:choose>
                    <c:when test="${sessionScope.user != null}">
                        <!-- Betting Panel -->
                        <div class="card betting-panel" style="position: sticky; top: 2rem;">
                            <div class="card-header">
                                <h4 style="margin: 0;">Place Your Bet</h4>
                            </div>
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${match.status == 'SCHEDULED'}">
                                        <div id="bettingForm" style="display: none;">
                                            <div class="alert alert-info">
                                                <span>Selected: <strong><span class="selected-team-display"></span></strong> @ <span class="selected-odds-display"></span></span>
                                            </div>

                                            <div class="form-group">
                                                <label class="form-label">Bet Amount ($)</label>
                                                <input type="number" class="form-control bet-amount-input"
                                                       placeholder="Enter amount" min="1" max="10000" step="0.01">
                                                <div class="form-text">Min: $1, Max: $10,000</div>
                                            </div>

                                            <div class="d-flex" style="gap: 0.5rem; margin-bottom: 1rem; flex-wrap: wrap;">
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="10">$10</button>
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="25">$25</button>
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="50">$50</button>
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="100">$100</button>
                                            </div>

                                            <div class="potential-winnings mb-3">
                                                <div>Potential Winnings</div>
                                                <div class="potential-winnings-amount">$0.00</div>
                                                <div class="potential-profit-amount">Profit: $0.00</div>
                                            </div>

                                            <button class="btn btn-success w-100 place-bet-btn">
                                                Place Bet
                                            </button>
                                        </div>

                                        <div id="teamSelection">
                                            <p class="text-center text-secondary">Select a team to start betting</p>
                                            <div style="display: flex; flex-direction: column; gap: 1rem;">
                                                <button class="btn btn-outline w-100 team-select-btn"
                                                        data-match-id="${match.id}"
                                                        data-team-id="${match.team1.id}"
                                                        data-team-name="${match.team1.teamName}"
                                                        data-odds="${match.team1Odds}">
                                                    ${match.team1.teamName} @ ${match.team1Odds}
                                                </button>
                                                <button class="btn btn-outline w-100 team-select-btn"
                                                        data-match-id="${match.id}"
                                                        data-team-id="${match.team2.id}"
                                                        data-team-name="${match.team2.teamName}"
                                                        data-odds="${match.team2Odds}">
                                                    ${match.team2.teamName} @ ${match.team2Odds}
                                                </button>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:when test="${match.status == 'LIVE'}">
                                        <div class="alert alert-warning">
                                            <span>🔴 Match is live! Betting is currently disabled.</span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-secondary">
                                            <span>✅ Match completed.
                                                <c:if test="${match.winnerTeam != null}">
                                                    Final result: ${match.winnerTeam.teamName} won ${match.team1Score}-${match.team2Score}
                                                </c:if>
                                            </span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- User's Bets on This Match -->
                        <c:if test="${not empty userBetsOnMatch}">
                            <div class="card mt-4">
                                <div class="card-header">
                                    <h4 style="margin: 0;">Your Bets on This Match</h4>
                                </div>
                                <div class="card-body">
                                    <c:forEach items="${userBetsOnMatch}" var="bet">
                                        <div style="border-bottom: 1px solid var(--border-color); padding: 1rem 0;">
                                            <div class="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <div style="font-weight: 600;">${bet.selectedTeam.teamName}</div>
                                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                        $<fmt:formatNumber value="${bet.betAmount}" pattern="#,##0.00"/> @ ${bet.oddsAtBet}
                                                    </div>
                                                </div>
                                                <div style="text-align: right;">
                                                    <div class="badge badge-${bet.status == 'WON' ? 'success' : bet.status == 'LOST' ? 'danger' : 'warning'}">
                                                        ${bet.status}
                                                    </div>
                                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                        Potential: $<fmt:formatNumber value="${bet.potentialWinnings}" pattern="#,##0.00"/>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <!-- Login Prompt -->
                        <div class="card">
                            <div class="card-header">
                                <h4 style="margin: 0;">Start Betting</h4>
                            </div>
                            <div class="card-body text-center">
                                <div style="padding: 2rem 1rem;">
                                    <h5>Join the Action!</h5>
                                    <p class="text-secondary">Create an account to place bets on this exciting match.</p>

                                    <div style="margin: 2rem 0;">
                                        <div class="stat-card" style="margin: 0;">
                                            <span class="stat-value">$100</span>
                                            <span class="stat-label">Welcome Bonus</span>
                                        </div>
                                    </div>

                                    <div style="display: flex; flex-direction: column; gap: 1rem;">
                                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary">
                                            Sign Up & Get $100
                                        </a>
                                        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline">
                                            Login to Account
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

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
        // Set user balance for JavaScript if user is logged in
        <c:if test="${sessionScope.user != null}">
            userBalance = ${sessionScope.user.walletBalance};
        </c:if>

        // Custom team selection for betting page
        document.querySelectorAll('.team-select-btn').forEach(button => {
            button.addEventListener('click', function() {
                // Remove previous selections
                document.querySelectorAll('.team-select-btn').forEach(btn => {
                    btn.classList.remove('selected');
                });

                // Select this team
                this.classList.add('selected');

                // Update betting panel
                const teamName = this.dataset.teamName;
                const odds = this.dataset.odds;

                document.querySelector('.selected-team-display').textContent = teamName;
                document.querySelector('.selected-odds-display').textContent = odds;

                // Show betting form
                document.getElementById('teamSelection').style.display = 'none';
                document.getElementById('bettingForm').style.display = 'block';

                // Store selection
                selectedMatch = this.dataset.matchId;
                selectedTeam = this.dataset.teamId;
            });
        });

        // Auto-refresh odds every 30 seconds
        setInterval(function() {
            refreshMatchData(${match.id});
        }, 30000);
    </script>
</body>
</html>