<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://esports.betting/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ML Betting - Mobile Legends Professional League Betting Platform</title>
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
                <li><a href="${pageContext.request.contextPath}/" class="nav-link active">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/matches/upcoming" class="nav-link">Matches</a></li>
                <c:choose>
                    <c:when test="${sessionScope.user != null}">
                        <li><a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a></li>
                        <li><a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a></li>
                        <li class="nav-item" style="color: var(--text-secondary); padding: 0.5rem 1rem;">
                            Balance: $<span class="user-balance"><fmt:formatNumber value="${sessionScope.user.walletBalance}" pattern="#,##0.00"/></span>
                        </li>
                        <li><a href="${pageContext.request.contextPath}/dashboard?action=logout" class="nav-link">Logout</a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="${pageContext.request.contextPath}/login" class="nav-link">Login</a></li>
                        <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Sign Up</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero">
        <div class="container">
            <div class="hero-content">
                <h1>Bet on Mobile Legends Professional League</h1>
                <p>Experience the excitement of esports betting with real-time odds, live matches, and instant payouts on the world's biggest Mobile Legends tournaments.</p>

                <div class="hero-stats">
                    <div class="stat">
                        <span class="stat-value">$2.5M+</span>
                        <span class="stat-label">Prize Pool</span>
                    </div>
                    <div class="stat">
                        <span class="stat-value">50K+</span>
                        <span class="stat-label">Active Bettors</span>
                    </div>
                    <div class="stat">
                        <span class="stat-value">99%</span>
                        <span class="stat-label">Payout Rate</span>
                    </div>
                </div>

                <div class="hero-cta">
                    <c:choose>
                        <c:when test="${sessionScope.user != null}">
                            <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary btn-large">
                                Go to Dashboard
                            </a>
                            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline btn-large">
                                View Matches
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-large">
                                Start Betting Now
                            </a>
                            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline btn-large">
                                View Matches
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </section>

    <!-- Live Matches Section -->
    <c:if test="${not empty liveMatches}">
        <section style="background: linear-gradient(135deg, var(--danger-color), #D63031); padding: 2rem 0;">
            <div class="container">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 style="color: white; margin: 0;">🔴 LIVE MATCHES</h2>
                    <a href="${pageContext.request.contextPath}/matches/live" style="color: white; text-decoration: underline;">View All Live</a>
                </div>

                <div class="stats-grid">
                    <c:forEach items="${liveMatches}" var="match" varStatus="status">
                        <c:if test="${status.index < 3}">
                            <div class="match-card live-match" data-match-id="${match.id}">
                                <div class="match-header">
                                    <span class="match-status status-live">LIVE</span>
                                    <span class="match-date" style="color: #fff;">NOW</span>
                                </div>

                                <div class="teams-container">
                                    <div class="team">
                                        <img src="https://via.placeholder.com/60x60/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                             alt="${match.team1.teamName}" class="team-logo">
                                        <div class="team-name">${match.team1.teamName}</div>
                                        <div class="team-score">${match.team1Score}</div>
                                    </div>

                                    <div class="vs-divider" style="color: #fff;">VS</div>

                                    <div class="team">
                                        <img src="https://via.placeholder.com/60x60/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                             alt="${match.team2.teamName}" class="team-logo">
                                        <div class="team-name">${match.team2.teamName}</div>
                                        <div class="team-score">${match.team2Score}</div>
                                    </div>
                                </div>

                                <div class="match-info">
                                    <span class="tournament-name" style="color: rgba(255,255,255,0.9);">
                                        <c:choose>
                                            <c:when test="${not empty match.tournament and not empty match.tournament.tournamentName}">
                                                ${match.tournament.tournamentName}
                                            </c:when>
                                            <c:otherwise>
                                                Tournament TBD
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                    <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-light btn-sm">
                                        Watch Live
                                    </a>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </div>
        </section>
    </c:if>

    <!-- Featured Matches Section -->
    <section class="container mt-5">
        <div class="section-header">
            <h2>🏆 Featured Matches</h2>
            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline">View All Matches</a>
        </div>

        <c:choose>
            <c:when test="${not empty featuredMatches}">
                <div class="stats-grid">
                    <c:forEach items="${featuredMatches}" var="match" varStatus="status">
                        <c:if test="${status.index < 6}">
                            <div class="match-card" data-match-id="${match.id}">
                                <div class="match-header">
                                    <span class="match-status ${match.status == 'LIVE' ? 'status-live' : match.status == 'COMPLETED' ? 'status-completed' : 'status-upcoming'}">
                                        ${match.status}
                                    </span>
                                    <span class="match-date">
                                        <c:choose>
                                            <c:when test="${match.status == 'LIVE'}">
                                                LIVE NOW
                                            </c:when>
                                            <c:when test="${match.status == 'COMPLETED'}">
                                                <fmt:formatDate value="${match.matchDateAsDate}" pattern="MMM dd" />
                                            </c:when>
                                            <c:otherwise>
                                                <span class="countdown" data-end-time="${match.matchDate}">
                                                    <fmt:formatDate value="${match.matchDateAsDate}" pattern="MMM dd, HH:mm" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>

                                <div class="teams-container">
                                    <div class="team">
                                        <img src="https://via.placeholder.com/60x60/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                             alt="${match.team1.teamName}" class="team-logo">
                                        <div class="team-name">${match.team1.teamName}</div>

                                        <c:choose>
                                            <c:when test="${match.status == 'COMPLETED'}">
                                                <div class="team-score ${match.winnerTeam.id == match.team1.id ? 'text-success' : 'text-secondary'}">
                                                    ${match.team1Score}
                                                </div>
                                            </c:when>
                                            <c:when test="${match.status == 'SCHEDULED' && sessionScope.user != null}">
                                                <button class="btn btn-sm btn-outline team-select-btn"
                                                        data-match-id="${match.id}"
                                                        data-team-id="${match.team1.id}"
                                                        data-team-name="${match.team1.teamName}"
                                                        data-odds="${match.team1Odds}">
                                                    ${match.team1Odds}
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="team-odds">${match.team1Odds}</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="vs-divider">
                                        <c:choose>
                                            <c:when test="${match.status == 'COMPLETED'}">
                                                <c:if test="${match.winnerTeam != null}">
                                                    <div style="font-size: 0.8rem; color: var(--success-color);">
                                                        ${match.winnerTeam.teamName == match.team1.teamName ? match.team1.teamCode : match.team2.teamCode} WON
                                                    </div>
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>VS</c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="team">
                                        <img src="https://via.placeholder.com/60x60/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                             alt="${match.team2.teamName}" class="team-logo">
                                        <div class="team-name">${match.team2.teamName}</div>

                                        <c:choose>
                                            <c:when test="${match.status == 'COMPLETED'}">
                                                <div class="team-score ${match.winnerTeam.id == match.team2.id ? 'text-success' : 'text-secondary'}">
                                                    ${match.team2Score}
                                                </div>
                                            </c:when>
                                            <c:when test="${match.status == 'SCHEDULED' && sessionScope.user != null}">
                                                <button class="btn btn-sm btn-outline team-select-btn"
                                                        data-match-id="${match.id}"
                                                        data-team-id="${match.team2.id}"
                                                        data-team-name="${match.team2.teamName}"
                                                        data-odds="${match.team2Odds}">
                                                    ${match.team2Odds}
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="team-odds">${match.team2Odds}</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- Match Info -->
                                <div class="match-info">
                                    <div>
                                        <span class="tournament-name">
                                            <c:choose>
                                                <c:when test="${not empty match.tournament and not empty match.tournament.tournamentName}">
                                                    ${match.tournament.tournamentName}
                                                </c:when>
                                                <c:otherwise>
                                                    Tournament TBD
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                            ${match.matchType} • Pool: $<fmt:formatNumber value="${match.totalPool}" pattern="#,##0"/>
                                        </div>
                                    </div>

                                    <div class="d-flex" style="gap: 0.5rem;">
                                        <c:if test="${match.status == 'LIVE'}">
                                            <button class="btn btn-danger btn-sm">Watch Live</button>
                                        </c:if>
                                        <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-primary btn-sm">
                                            Details
                                        </a>
                                    </div>
                                </div>

                                <!-- Quick Betting Panel for upcoming matches -->
                                <c:if test="${match.status == 'SCHEDULED' && sessionScope.user != null}">
                                    <div class="betting-panel" style="display: none;">
                                        <h5>Place Your Bet</h5>
                                        <p>Selected: <span class="selected-team-display"></span> @ <span class="selected-odds-display"></span></p>

                                        <div class="betting-form">
                                            <div class="form-group">
                                                <label class="form-label">Bet Amount</label>
                                                <input type="number" class="form-control bet-amount-input"
                                                       placeholder="Enter amount" min="1" max="10000" step="0.01">
                                            </div>

                                            <div class="d-flex" style="gap: 0.5rem; margin-bottom: 1rem;">
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="10">$10</button>
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="25">$25</button>
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="50">$50</button>
                                                <button class="btn btn-sm btn-outline quick-bet-btn" data-amount="100">$100</button>
                                            </div>

                                            <button class="btn btn-primary place-bet-btn w-100">Place Bet</button>
                                        </div>

                                        <div class="potential-winnings">
                                            <div>Potential Winnings</div>
                                            <div class="potential-winnings-amount">$0.00</div>
                                            <div class="potential-profit-amount">Profit: $0.00</div>
                                        </div>
                                    </div>
                                </c:if>

                                <!-- Login prompt for guests -->
                                <c:if test="${match.status == 'SCHEDULED' && sessionScope.user == null}">
                                    <div style="background: rgba(108, 92, 231, 0.1); border-radius: 8px; padding: 1rem; margin-top: 1rem; text-align: center;">
                                        <p style="margin: 0; color: var(--text-secondary);">
                                            <a href="${pageContext.request.contextPath}/login" style="color: var(--primary-color); font-weight: 600;">Login</a>
                                            or
                                            <a href="${pageContext.request.contextPath}/register" style="color: var(--primary-color); font-weight: 600;">Sign up</a>
                                            to place bets
                                        </p>
                                    </div>
                                </c:if>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center" style="padding: 4rem 2rem;">
                    <h3>No matches available</h3>
                    <p class="text-secondary">New matches are being scheduled. Check back soon!</p>
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-warning">
                            <span>System Status: ${serviceStatus} | Database: ${dbStatus}</span>
                        </div>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- Tournament Information -->
    <section class="container mt-5">
        <div class="card">
            <div class="card-header">
                <h3 style="margin: 0;">🏆 Current Tournament: MPL Season 12</h3>
            </div>
            <div class="card-body">
                <div class="stats-grid">
                    <div class="stat-card">
                        <span class="stat-value">$500K</span>
                        <span class="stat-label">Prize Pool</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">8</span>
                        <span class="stat-label">Teams</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">156</span>
                        <span class="stat-label">Total Matches</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">BO3</span>
                        <span class="stat-label">Format</span>
                    </div>
                </div>

                <div style="margin-top: 2rem;">
                    <h5>Championship Features</h5>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 2rem; margin-top: 1rem;">
                        <div>
                            <h6>🎮 Live Betting</h6>
                            <p>Bet on matches while they're happening with real-time odds updates</p>
                        </div>
                        <div>
                            <h6>💰 Instant Payouts</h6>
                            <p>Get your winnings instantly credited to your account after match completion</p>
                        </div>
                        <div>
                            <h6>📊 Advanced Statistics</h6>
                            <p>Access detailed team performance data and historical match results</p>
                        </div>
                        <div>
                            <h6>🔒 Secure Platform</h6>
                            <p>Your funds and personal information are protected with bank-level security</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Call to Action -->
    <c:if test="${sessionScope.user == null}">
        <section style="background: linear-gradient(135deg, var(--primary-color), var(--secondary-color)); padding: 4rem 0; margin-top: 3rem;">
            <div class="container text-center">
                <h2 style="color: white; margin-bottom: 1rem;">Ready to Start Betting?</h2>
                <p style="color: rgba(255,255,255,0.9); font-size: 1.2rem; margin-bottom: 2rem;">
                    Join thousands of esports fans betting on Mobile Legends Professional League
                </p>

                <div style="display: flex; justify-content: center; gap: 2rem; margin-bottom: 2rem;">
                    <div style="color: white; text-align: center;">
                        <div style="font-size: 2rem; font-weight: 700;">$100</div>
                        <div>Welcome Bonus</div>
                    </div>
                    <div style="color: white; text-align: center;">
                        <div style="font-size: 2rem; font-weight: 700;">24/7</div>
                        <div>Live Support</div>
                    </div>
                    <div style="color: white; text-align: center;">
                        <div style="font-size: 2rem; font-weight: 700;">1 Min</div>
                        <div>Quick Signup</div>
                    </div>
                </div>

                <a href="${pageContext.request.contextPath}/register" class="btn btn-light btn-large">
                    Sign Up & Get $100 Bonus
                </a>
            </div>
        </section>
    </c:if>

    <!-- Footer -->
    <footer class="footer">
        <div class="container">
            <div class="footer-content">
                <div class="footer-section">
                    <h5>Mobile Legends Betting</h5>
                    <p>The premier platform for Mobile Legends Professional League betting. Experience the thrill of esports with competitive odds and instant payouts.</p>
                </div>

                <div class="footer-section">
                    <h5>Quick Links</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/matches/upcoming">Upcoming Matches</a>
                        <a href="${pageContext.request.contextPath}/matches/live">Live Matches</a>
                        <a href="${pageContext.request.contextPath}/results">Match Results</a>
                        <a href="${pageContext.request.contextPath}/teams">Teams & Players</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Support</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="#">How to Bet</a>
                        <a href="#">Payment Methods</a>
                        <a href="#">Responsible Gaming</a>
                        <a href="#">Contact Support</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Tournament Info</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <span>MPL Season 12</span>
                        <span>Prize Pool: $500,000</span>
                        <span>8 Teams Competing</span>
                        <span>Live Daily Matches</span>
                    </div>
                </div>
            </div>

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

        // Auto-refresh live matches every 30 seconds
        <c:if test="${not empty liveMatches}">
            setInterval(function() {
                // Refresh live match data
                fetch('${pageContext.request.contextPath}/matches/live?format=json')
                    .then(response => response.json())
                    .then(data => {
                        // Update live match cards with new data
                        console.log('Live matches updated');
                    })
                    .catch(error => console.error('Error updating live matches:', error));
            }, 30000);
        </c:if>

        // Initialize countdown timers
        document.addEventListener('DOMContentLoaded', function() {
            updateCountdowns();
            setInterval(updateCountdowns, 1000);
        });

        function updateCountdowns() {
            const countdownElements = document.querySelectorAll('.countdown');

            countdownElements.forEach(element => {
                const endTime = new Date(element.dataset.endTime).getTime();
                const now = new Date().getTime();
                const distance = endTime - now;

                if (distance < 0) {
                    element.textContent = 'Started';
                    element.classList.add('expired');
                    return;
                }

                const hours = Math.floor(distance / (1000 * 60 * 60));
                const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));

                if (hours > 0) {
                    element.textContent = `in ${hours}h ${minutes}m`;
                } else if (minutes > 0) {
                    element.textContent = `in ${minutes}m`;
                } else {
                    element.textContent = 'Starting soon';
                }
            });
        }
    </script>
</body>
</html>