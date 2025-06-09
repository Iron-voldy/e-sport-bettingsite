<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : 'Matches'} - ML Betting</title>
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
                <li><a href="${pageContext.request.contextPath}/matches/upcoming" class="nav-link active">Matches</a></li>
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

    <!-- Page Header -->
    <section class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>${pageTitle != null ? pageTitle : 'Matches'}</h1>
                <p class="text-secondary">Mobile Legends Professional League Championship</p>
            </div>

            <!-- Search Bar -->
            <div style="max-width: 300px; width: 100%; position: relative;">
                <input type="text" id="searchInput" class="form-control" placeholder="Search teams or tournaments..." style="padding-left: 40px;">
                <span style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--text-muted);">🔍</span>
            </div>
        </div>

        <!-- Filter Tabs -->
        <div class="d-flex" style="gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem;">
            <a href="${pageContext.request.contextPath}/matches/upcoming"
               class="btn ${matchType == 'upcoming' || matchType == null ? 'btn-primary' : 'btn-outline'}">
                ⏰ Upcoming
            </a>
            <a href="${pageContext.request.contextPath}/matches/live"
               class="btn ${matchType == 'live' ? 'btn-danger' : 'btn-outline'}">
                🔴 Live
            </a>
            <a href="${pageContext.request.contextPath}/matches/completed"
               class="btn ${matchType == 'completed' ? 'btn-success' : 'btn-outline'}">
                ✅ Completed
            </a>
        </div>

        <!-- Display Error Messages -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <span>${errorMessage}</span>
            </div>
        </c:if>
    </section>

    <!-- Live Matches Banner -->
    <c:if test="${matchType == 'live' || (matchType == null && not empty liveMatches)}">
        <section style="background: linear-gradient(135deg, var(--danger-color), #D63031); padding: 1rem 0; margin-bottom: 2rem;">
            <div class="container">
                <div class="d-flex justify-content-between align-items-center">
                    <div style="color: white;">
                        <h3 style="margin: 0;">🔴 LIVE MATCHES</h3>
                        <p style="margin: 0; opacity: 0.9;">Bet now with live odds!</p>
                    </div>
                    <div style="color: white; font-size: 2rem; animation: pulse 2s infinite;">
                        📺
                    </div>
                </div>
            </div>
        </section>
    </c:if>

    <!-- Matches Grid -->
    <section class="container">
        <c:choose>
            <c:when test="${not empty matches}">
                <div class="stats-grid">
                    <c:forEach items="${matches}" var="match">
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
                                            <div class="team-score ${match.winnerTeam != null && match.winnerTeam.id == match.team1.id ? 'text-success' : 'text-secondary'}">
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
                                            <div class="team-score ${match.winnerTeam != null && match.winnerTeam.id == match.team2.id ? 'text-success' : 'text-secondary'}">
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
                                    <span class="tournament-name">${match.tournamentName}</span>
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

                            <!-- Betting Panel (for upcoming matches) -->
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
                    </c:forEach>
                </div>

                <!-- Load More Button -->
                <c:if test="${matches.size() >= 12}">
                    <div class="text-center mt-4">
                        <button class="btn btn-outline" id="loadMoreBtn">
                            Load More Matches
                        </button>
                    </div>
                </c:if>
            </c:when>

            <c:otherwise>
                <div class="text-center" style="padding: 4rem 2rem;">
                    <h3>No ${matchType != null ? matchType : ''} matches found</h3>
                    <p class="text-secondary">
                        <c:choose>
                            <c:when test="${matchType == 'live'}">
                                No matches are currently live. Check back soon!
                            </c:when>
                            <c:when test="${matchType == 'completed'}">
                                No completed matches to display.
                            </c:when>
                            <c:otherwise>
                                No upcoming matches scheduled. New matches are added regularly.
                            </c:otherwise>
                        </c:choose>
                    </p>
                    <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-primary">
                        View All Matches
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- Tournament Info Section -->
    <section class="container mt-5">
        <div class="card">
            <div class="card-header">
                <h3 style="margin: 0;">Tournament Information</h3>
            </div>
            <div class="card-body">
                <div class="stats-grid">
                    <div class="stat-card">
                        <span class="stat-value">MPL S12</span>
                        <span class="stat-label">Current Season</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">$500K</span>
                        <span class="stat-label">Prize Pool</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">8</span>
                        <span class="stat-label">Teams</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">BO3</span>
                        <span class="stat-label">Match Format</span>
                    </div>
                </div>
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

        // Load more matches functionality
        document.getElementById('loadMoreBtn')?.addEventListener('click', async function() {
            const btn = this;
            setButtonLoading(btn, true);

            try {
                // Simulate loading more matches
                setTimeout(() => {
                    setButtonLoading(btn, false);
                    showAlert('All available matches are loaded', 'info');
                    btn.style.display = 'none';
                }, 1000);
            } catch (error) {
                setButtonLoading(btn, false);
                showAlert('Error loading more matches', 'danger');
            }
        });

        // Auto-refresh live matches every 30 seconds
        <c:if test="${matchType == 'live'}">
            setInterval(function() {
                // Only refresh if there are live matches
                const liveMatches = document.querySelectorAll('.status-live');
                if (liveMatches.length > 0) {
                    refreshAllMatches();
                }
            }, 30000);
        </c:if>

        function refreshAllMatches() {
            // Refresh match data
            console.log('Refreshing live matches...');
        }
    </script>
</body>
</html>