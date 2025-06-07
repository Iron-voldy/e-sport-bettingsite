<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Match Results - ML Betting</title>
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
                    <li><a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a></li>
                    <li><a href="${pageContext.request.contextPath}/dashboard?action=logout" class="nav-link">Logout</a></li>
                </c:if>
                <c:if test="${sessionScope.user == null}">
                    <li><a href="${pageContext.request.contextPath}/login" class="nav-link">Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Sign Up</a></li>
                </c:if>
            </ul>
        </div>
    </nav>

    <!-- Page Header -->
    <section class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Match Results</h1>
                <p class="text-secondary">Mobile Legends Professional League Championship Results</p>
            </div>

            <!-- Filter Controls -->
            <div class="d-flex" style="gap: 1rem;">
                <select class="form-control" id="tournamentFilter" style="width: auto;">
                    <option value="">All Tournaments</option>
                    <option value="mpl-s12">MPL Season 12</option>
                    <option value="mpl-playoffs">MPL Playoffs</option>
                    <option value="world-championship">World Championship</option>
                </select>

                <select class="form-control" id="dateFilter" style="width: auto;">
                    <option value="">All Time</option>
                    <option value="today">Today</option>
                    <option value="week">This Week</option>
                    <option value="month">This Month</option>
                </select>
            </div>
        </div>
    </section>

    <!-- Tournament Standings -->
    <section class="container mb-5">
        <div class="card">
            <div class="card-header">
                <h3 style="margin: 0;">Current Tournament Standings</h3>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Rank</th>
                                <th>Team</th>
                                <th>Matches</th>
                                <th>Wins</th>
                                <th>Losses</th>
                                <th>Win Rate</th>
                                <th>Points</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr style="background: rgba(0, 184, 148, 0.1);">
                                <td><span class="badge badge-success">1</span></td>
                                <td>
                                    <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                        <img src="https://via.placeholder.com/30x30/FF6B6B/FFFFFF?text=BLI"
                                             alt="Blacklist" style="width: 30px; height: 30px; border-radius: 50%;">
                                        <strong>Blacklist International</strong>
                                    </div>
                                </td>
                                <td>14</td>
                                <td>12</td>
                                <td>2</td>
                                <td>85.7%</td>
                                <td><strong>36</strong></td>
                            </tr>
                            <tr>
                                <td><span class="badge badge-primary">2</span></td>
                                <td>
                                    <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                        <img src="https://via.placeholder.com/30x30/00D2D3/FFFFFF?text=RRQ"
                                             alt="RRQ" style="width: 30px; height: 30px; border-radius: 50%;">
                                        <strong>RRQ Hoshi</strong>
                                    </div>
                                </td>
                                <td>14</td>
                                <td>10</td>
                                <td>4</td>
                                <td>71.4%</td>
                                <td><strong>30</strong></td>
                            </tr>
                            <tr>
                                <td><span class="badge badge-warning">3</span></td>
                                <td>
                                    <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                        <img src="https://via.placeholder.com/30x30/6C5CE7/FFFFFF?text=ONIC"
                                             alt="ONIC" style="width: 30px; height: 30px; border-radius: 50%;">
                                        <strong>ONIC Esports</strong>
                                    </div>
                                </td>
                                <td>14</td>
                                <td>9</td>
                                <td>5</td>
                                <td>64.3%</td>
                                <td><strong>27</strong></td>
                            </tr>
                            <tr>
                                <td>4</td>
                                <td>
                                    <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                        <img src="https://via.placeholder.com/30x30/FDCB6E/FFFFFF?text=EVOS"
                                             alt="EVOS" style="width: 30px; height: 30px; border-radius: 50%;">
                                        <strong>EVOS Legends</strong>
                                    </div>
                                </td>
                                <td>14</td>
                                <td>8</td>
                                <td>6</td>
                                <td>57.1%</td>
                                <td><strong>24</strong></td>
                            </tr>
                            <tr>
                                <td>5</td>
                                <td>
                                    <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                        <img src="https://via.placeholder.com/30x30/E17055/FFFFFF?text=ECHO"
                                             alt="Echo" style="width: 30px; height: 30px; border-radius: 50%;">
                                        <strong>Echo</strong>
                                    </div>
                                </td>
                                <td>14</td>
                                <td>6</td>
                                <td>8</td>
                                <td>42.9%</td>
                                <td><strong>18</strong></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </section>

    <!-- Recent Match Results -->
    <section class="container">
        <div class="card">
            <div class="card-header">
                <h3 style="margin: 0;">Recent Match Results</h3>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty completedMatches}">
                        <div class="stats-grid">
                            <c:forEach items="${completedMatches}" var="match">
                                <div class="match-card" data-match-id="${match.id}">
                                    <div class="match-header">
                                        <span class="match-status status-completed">COMPLETED</span>
                                        <span class="match-date">
                                            <fmt:formatDate value="${match.matchDate}" pattern="MMM dd, HH:mm" />
                                        </span>
                                    </div>

                                    <div class="teams-container">
                                        <div class="team">
                                            <img src="https://via.placeholder.com/60x60/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                                 alt="${match.team1.teamName}" class="team-logo">
                                            <div class="team-name">${match.team1.teamName}</div>
                                            <div class="team-score ${match.winnerTeam.id == match.team1.id ? 'text-success' : 'text-secondary'}" style="font-size: 2rem; font-weight: 700;">
                                                ${match.team1Score}
                                            </div>
                                        </div>

                                        <div class="vs-divider">
                                            <div style="font-size: 0.8rem; color: var(--success-color); text-align: center;">
                                                ${match.winnerTeam.teamCode} WON
                                            </div>
                                        </div>

                                        <div class="team">
                                            <img src="https://via.placeholder.com/60x60/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                                 alt="${match.team2.teamName}" class="team-logo">
                                            <div class="team-name">${match.team2.teamName}</div>
                                            <div class="team-score ${match.winnerTeam.id == match.team2.id ? 'text-success' : 'text-secondary'}" style="font-size: 2rem; font-weight: 700;">
                                                ${match.team2Score}
                                            </div>
                                        </div>
                                    </div>

                                    <div class="match-info">
                                        <div>
                                            <span class="tournament-name">${match.tournament.tournamentName}</span>
                                            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                ${match.matchType} • Winner: ${match.winnerTeam.teamName}
                                            </div>
                                        </div>

                                        <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-outline btn-sm">
                                            View Details
                                        </a>
                                    </div>

                                    <!-- Show user's bet result if logged in -->
                                    <c:if test="${sessionScope.user != null}">
                                        <c:forEach items="${userBets}" var="bet">
                                            <c:if test="${bet.match.id == match.id}">
                                                <div style="background: ${bet.status == 'WON' ? 'rgba(0, 184, 148, 0.1)' : 'rgba(225, 112, 85, 0.1)'};
                                                            border-radius: 8px; padding: 1rem; margin-top: 1rem;">
                                                    <div class="d-flex justify-content-between align-items-center">
                                                        <div>
                                                            <div style="font-weight: 600;">Your Bet: ${bet.selectedTeam.teamName}</div>
                                                            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                                $${bet.betAmount} @ ${bet.oddsAtBet}
                                                            </div>
                                                        </div>
                                                        <div style="text-align: right;">
                                                            <div class="badge badge-${bet.status == 'WON' ? 'success' : 'danger'}">
                                                                ${bet.status}
                                                            </div>
                                                            <div style="font-weight: 600; color: ${bet.status == 'WON' ? 'var(--success-color)' : 'var(--danger-color)'};">
                                                                ${bet.status == 'WON' ? '+' : '-'}$${bet.status == 'WON' ? bet.potentialWinnings : bet.betAmount}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </div>

                        <!-- Load More Button -->
                        <div class="text-center mt-4">
                            <button class="btn btn-outline" id="loadMoreResults">
                                Load More Results
                            </button>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center" style="padding: 4rem 2rem;">
                            <h3>No match results available</h3>
                            <p class="text-secondary">Match results will appear here once games are completed.</p>
                            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-primary">
                                View Upcoming Matches
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <!-- Statistics Section -->
    <section class="container mt-5">
        <div class="card">
            <div class="card-header">
                <h3 style="margin: 0;">Tournament Statistics</h3>
            </div>
            <div class="card-body">
                <div class="stats-grid">
                    <div class="stat-card">
                        <span class="stat-value">156</span>
                        <span class="stat-label">Total Matches</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">$2.5M</span>
                        <span class="stat-label">Total Prize Pool</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">12,500</span>
                        <span class="stat-label">Total Bets Placed</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-value">$890K</span>
                        <span class="stat-label">Total Winnings Paid</span>
                    </div>
                </div>

                <!-- Performance Charts -->
                <div style="margin-top: 2rem;">
                    <h5>Team Performance Over Time</h5>
                    <canvas id="teamPerformanceChart" width="800" height="300"></canvas>
                </div>
            </div>
        </div>
    </section>

    <!-- Top Performers -->
    <section class="container mt-5">
        <div class="d-flex" style="gap: 2rem;">
            <!-- Top Betting Winners -->
            <div class="card" style="flex: 1;">
                <div class="card-header">
                    <h4 style="margin: 0;">🏆 Top Betting Winners</h4>
                </div>
                <div class="card-body">
                    <div style="display: flex; flex-direction: column; gap: 1rem;">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                <span style="font-size: 1.5rem;">🥇</span>
                                <div>
                                    <div style="font-weight: 600;">Player****2024</div>
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">15 wins this month</div>
                                </div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 600; color: var(--success-color);">+$2,450</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">85% win rate</div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center">
                            <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                <span style="font-size: 1.5rem;">🥈</span>
                                <div>
                                    <div style="font-weight: 600;">Gamer****PH</div>
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">12 wins this month</div>
                                </div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 600; color: var(--success-color);">+$1,890</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">75% win rate</div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center">
                            <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                <span style="font-size: 1.5rem;">🥉</span>
                                <div>
                                    <div style="font-weight: 600;">MLFan****123</div>
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">10 wins this month</div>
                                </div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 600; color: var(--success-color);">+$1,520</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">71% win rate</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Most Popular Bets -->
            <div class="card" style="flex: 1;">
                <div class="card-header">
                    <h4 style="margin: 0;">📊 Most Popular Bets</h4>
                </div>
                <div class="card-body">
                    <div style="display: flex; flex-direction: column; gap: 1rem;">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div style="font-weight: 600;">Blacklist International</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">vs ONIC Esports</div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 600;">1,250 bets</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">$45,600 pool</div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div style="font-weight: 600;">RRQ Hoshi</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">vs EVOS Legends</div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 600;">980 bets</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">$38,200 pool</div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div style="font-weight: 600;">Echo</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">vs Team Flash</div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 600;">756 bets</div>
                                <div style="font-size: 0.875rem; color: var(--text-secondary);">$29,800 pool</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
        <div class="container">
            <div class="footer-content">
                <div class="footer-section">
                    <h5>Match Results</h5>
                    <p>Stay updated with the latest Mobile Legends Professional League match results, standings, and statistics.</p>
                </div>

                <div class="footer-section">
                    <h5>Tournament Info</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <span>Season 12 Results</span>
                        <span>Playoff Standings</span>
                        <span>Championship Results</span>
                        <span>Historical Data</span>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Statistics</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <span>Team Performance</span>
                        <span>Player Stats</span>
                        <span>Betting Analytics</span>
                        <span>Win Predictions</span>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Follow Matches</h5>
                    <p>Get real-time updates on match results and tournament standings.</p>
                    <div style="margin-top: 1rem;">
                        <span class="badge badge-success">Live Updates</span>
                        <span class="badge badge-primary">Real-time</span>
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
        // Initialize team performance chart
        const teamChart = document.getElementById('teamPerformanceChart');
        if (teamChart) {
            const ctx = teamChart.getContext('2d');

            // Sample team performance data
            const teams = ['Blacklist', 'RRQ', 'ONIC', 'EVOS', 'Echo'];
            const colors = ['#FF6B6B', '#00D2D3', '#6C5CE7', '#FDCB6E', '#E17055'];
            const performanceData = [
                [85, 87, 90, 88, 92], // Blacklist
                [75, 78, 76, 80, 82], // RRQ
                [70, 72, 75, 74, 76], // ONIC
                [65, 68, 66, 70, 69], // EVOS
                [55, 58, 60, 62, 64]  // Echo
            ];

            // Draw background grid
            ctx.strokeStyle = 'rgba(160, 163, 189, 0.1)';
            ctx.lineWidth = 1;
            for (let i = 1; i < 10; i++) {
                ctx.beginPath();
                ctx.moveTo(0, (i / 10) * teamChart.height);
                ctx.lineTo(teamChart.width, (i / 10) * teamChart.height);
                ctx.stroke();
            }

            // Draw team performance lines
            teams.forEach((team, teamIndex) => {
                ctx.strokeStyle = colors[teamIndex];
                ctx.lineWidth = 2;
                ctx.beginPath();

                performanceData[teamIndex].forEach((performance, index) => {
                    const x = (index / (performanceData[teamIndex].length - 1)) * teamChart.width;
                    const y = teamChart.height - (performance / 100) * teamChart.height;

                    if (index === 0) ctx.moveTo(x, y);
                    else ctx.lineTo(x, y);
                });
                ctx.stroke();

                // Add team name to legend
                ctx.fillStyle = colors[teamIndex];
                ctx.fillRect(10, 10 + (teamIndex * 20), 15, 10);
                ctx.fillStyle = '#FFFFFF';
                ctx.font = '12px Inter';
                ctx.fillText(team, 30, 20 + (teamIndex * 20));
            });
        }

        // Filter functionality
        document.getElementById('tournamentFilter').addEventListener('change', function() {
            const selectedTournament = this.value;
            filterResults();
        });

        document.getElementById('dateFilter').addEventListener('change', function() {
            const selectedDate = this.value;
            filterResults();
        });

        function filterResults() {
            const tournamentFilter = document.getElementById('tournamentFilter').value;
            const dateFilter = document.getElementById('dateFilter').value;

            // Simulate filtering
            showAlert('Filters applied successfully!', 'info');
        }

        // Load more results
        document.getElementById('loadMoreResults')?.addEventListener('click', function() {
            const btn = this;
            setButtonLoading(btn, true);

            setTimeout(() => {
                setButtonLoading(btn, false);
                showAlert('All available results are loaded', 'info');
                btn.style.display = 'none';
            }, 1000);
        });

        // Auto-refresh results every 2 minutes
        setInterval(function() {
            // Check for new completed matches
            fetch('${pageContext.request.contextPath}/matches/completed?format=json')
                .then(response => response.json())
                .then(data => {
                    // Update results if new matches completed
                    console.log('Checking for new results...');
                })
                .catch(error => console.error('Error checking for updates:', error));
        }, 120000);
    </script>
</body>
</html>