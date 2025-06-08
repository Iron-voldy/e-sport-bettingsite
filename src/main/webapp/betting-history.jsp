<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Betting History - ML Betting</title>
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
                <li><a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a></li>
                <li class="nav-item" style="color: var(--text-secondary); padding: 0.5rem 1rem;">
                    Balance: $<span class="user-balance"><fmt:formatNumber value="${sessionScope.user.walletBalance}" pattern="#,##0.00"/></span>
                </li>
                <li><a href="${pageContext.request.contextPath}/dashboard?action=logout" class="nav-link">Logout</a></li>
            </ul>
        </div>
    </nav>

    <!-- Page Header -->
    <section class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Betting History</h1>
                <p class="text-secondary">Track all your betting activity and performance</p>
            </div>

            <!-- Filter Controls -->
            <div class="d-flex" style="gap: 1rem;">
                <select class="form-control" id="statusFilter" style="width: auto;">
                    <option value="">All Bets</option>
                    <option value="PENDING" ${filterStatus == 'pending' ? 'selected' : ''}>Pending</option>
                    <option value="WON" ${filterStatus == 'won' ? 'selected' : ''}>Won</option>
                    <option value="LOST" ${filterStatus == 'lost' ? 'selected' : ''}>Lost</option>
                    <option value="CANCELLED" ${filterStatus == 'cancelled' ? 'selected' : ''}>Cancelled</option>
                </select>

                <select class="form-control" id="dateFilter" style="width: auto;">
                    <option value="">All Time</option>
                    <option value="today">Today</option>
                    <option value="week">This Week</option>
                    <option value="month">This Month</option>
                </select>
            </div>
        </div>

        <!-- Summary Cards -->
        <div class="stats-grid mb-4">
            <div class="stat-card">
                <span class="stat-value">${userStats.totalBets != null ? userStats.totalBets : 0}</span>
                <span class="stat-label">Total Bets</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">$<fmt:formatNumber value="${userStats.totalBetAmount != null ? userStats.totalBetAmount : 0}" pattern="#,##0.00"/></span>
                <span class="stat-label">Total Wagered</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">$<fmt:formatNumber value="${userStats.totalWinnings != null ? userStats.totalWinnings : 0}" pattern="#,##0.00"/></span>
                <span class="stat-label">Total Winnings</span>
            </div>
            <div class="stat-card">
                <span class="stat-value"><fmt:formatNumber value="${userStats.winRate != null ? userStats.winRate : 0}" pattern="#0.0"/>%</span>
                <span class="stat-label">Win Rate</span>
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

    <!-- Betting History Table -->
    <section class="container">
        <div class="card">
            <div class="card-header">
                <div class="d-flex justify-content-between align-items-center">
                    <h3 style="margin: 0;">Your Betting History</h3>
                    <div class="d-flex" style="gap: 1rem;">
                        <button class="btn btn-sm btn-outline" onclick="exportHistory()">Export CSV</button>
                        <button class="btn btn-sm btn-primary" onclick="refreshHistory()">Refresh</button>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty bets}">
                        <div class="table-responsive">
                            <table class="table" id="bettingTable">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Match</th>
                                        <th>Team Bet</th>
                                        <th>Amount</th>
                                        <th>Odds</th>
                                        <th>Potential</th>
                                        <th>Status</th>
                                        <th>Result</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${bets}" var="bet" varStatus="status">
                                        <tr class="bet-row" data-bet-id="${bet.id}" data-status="${bet.status}">
                                            <td>
                                                <div style="font-size: 0.875rem;">
                                                    <div><fmt:formatDate value="${bet.betPlacedAt}" pattern="MMM dd, yyyy" /></div>
                                                    <div class="text-muted"><fmt:formatDate value="${bet.betPlacedAt}" pattern="HH:mm" /></div>
                                                </div>
                                            </td>
                                            <td>
                                                <div style="min-width: 200px;">
                                                    <div style="font-weight: 600; margin-bottom: 0.25rem;">
                                                        ${bet.match.team1.teamName} vs ${bet.match.team2.teamName}
                                                    </div>
                                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                        ${bet.match.tournament.tournamentName}
                                                    </div>
                                                    <div style="font-size: 0.75rem; color: var(--text-muted);">
                                                        <fmt:formatDate value="${bet.match.matchDate}" pattern="MMM dd, HH:mm" />
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <div class="d-flex align-items-center" style="gap: 0.5rem;">
                                                    <img src="https://via.placeholder.com/24x24/6C5CE7/FFFFFF?text=${bet.selectedTeam.teamCode.substring(0,1)}"
                                                         alt="${bet.selectedTeam.teamName}" style="width: 24px; height: 24px; border-radius: 50%;">
                                                    <strong>${bet.selectedTeam.teamName}</strong>
                                                </div>
                                            </td>
                                            <td>
                                                <span style="font-weight: 600;">$<fmt:formatNumber value="${bet.betAmount}" pattern="#,##0.00"/></span>
                                            </td>
                                            <td>
                                                <span class="team-odds">${bet.oddsAtBet}</span>
                                            </td>
                                            <td>
                                                <span style="font-weight: 600; color: var(--success-color);">
                                                    $<fmt:formatNumber value="${bet.potentialWinnings}" pattern="#,##0.00"/>
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge badge-${bet.status == 'WON' ? 'success' : bet.status == 'LOST' ? 'danger' : bet.status == 'CANCELLED' ? 'secondary' : 'warning'}">
                                                    ${bet.status}
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${bet.status == 'WON'}">
                                                        <div style="color: var(--success-color); font-weight: 600;">
                                                            +$<fmt:formatNumber value="${bet.potentialWinnings}" pattern="#,##0.00"/>
                                                        </div>
                                                        <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                            Profit: +$<fmt:formatNumber value="${bet.potentialWinnings - bet.betAmount}" pattern="#,##0.00"/>
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${bet.status == 'LOST'}">
                                                        <div style="color: var(--danger-color); font-weight: 600;">
                                                            -$<fmt:formatNumber value="${bet.betAmount}" pattern="#,##0.00"/>
                                                        </div>
                                                        <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                            Lost bet
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${bet.status == 'CANCELLED'}">
                                                        <div style="color: var(--text-muted); font-weight: 600;">
                                                            Refunded
                                                        </div>
                                                        <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                            $<fmt:formatNumber value="${bet.betAmount}" pattern="#,##0.00"/>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div style="color: var(--warning-color); font-weight: 600;">
                                                            Pending
                                                        </div>
                                                        <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                            Match not finished
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="d-flex" style="gap: 0.5rem;">
                                                    <a href="${pageContext.request.contextPath}/matches/details/${bet.match.id}"
                                                       class="btn btn-sm btn-outline" title="View Match">
                                                        👁️
                                                    </a>
                                                    <c:if test="${bet.status == 'PENDING' && bet.canBeCancelled}">
                                                        <button class="btn btn-sm btn-danger"
                                                                onclick="cancelBet(${bet.id})" title="Cancel Bet">
                                                            ❌
                                                        </button>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <!-- Pagination -->
                        <c:if test="${bets.size() >= 20}">
                            <div class="d-flex justify-content-between align-items-center mt-4">
                                <div style="color: var(--text-secondary);">
                                    Showing ${bets.size()} bets
                                </div>
                                <div class="d-flex" style="gap: 0.5rem;">
                                    <button class="btn btn-sm btn-outline" id="prevPage">Previous</button>
                                    <button class="btn btn-sm btn-outline" id="nextPage">Next</button>
                                </div>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center" style="padding: 4rem 2rem;">
                            <div style="font-size: 4rem; margin-bottom: 1rem;">🎲</div>
                            <h3>No betting history yet</h3>
                            <p class="text-secondary">Start betting on matches to see your history here.</p>
                            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-primary">
                                View Available Matches
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <!-- Performance Analysis -->
    <c:if test="${not empty bets}">
        <section class="container mt-5">
            <div class="card">
                <div class="card-header">
                    <h3 style="margin: 0;">Performance Analysis</h3>
                </div>
                <div class="card-body">
                    <div class="d-flex" style="gap: 2rem;">
                        <!-- Performance Chart -->
                        <div style="flex: 2;">
                            <h5>Profit/Loss Over Time</h5>
                            <canvas id="performanceChart" width="600" height="300"></canvas>
                        </div>

                        <!-- Statistics -->
                        <div style="flex: 1;">
                            <h5>Betting Patterns</h5>
                            <div style="display: flex; flex-direction: column; gap: 1rem;">
                                <div class="d-flex justify-content-between">
                                    <span>Average Bet:</span>
                                    <strong>$<fmt:formatNumber value="${userStats.totalBetAmount / userStats.totalBets}" pattern="#,##0.00"/></strong>
                                </div>
                                <div class="d-flex justify-content-between">
                                    <span>Biggest Win:</span>
                                    <strong class="text-success">$250.00</strong>
                                </div>
                                <div class="d-flex justify-content-between">
                                    <span>Longest Streak:</span>
                                    <strong>5 wins</strong>
                                </div>
                                <div class="d-flex justify-content-between">
                                    <span>Favorite Team:</span>
                                    <strong>Blacklist Intl</strong>
                                </div>
                                <div class="d-flex justify-content-between">
                                    <span>Best Odds Hit:</span>
                                    <strong>3.25</strong>
                                </div>
                                <div class="d-flex justify-content-between">
                                    <span>Total Profit:</span>
                                    <strong class="${userStats.totalWinnings - userStats.totalBetAmount >= 0 ? 'text-success' : 'text-danger'}">
                                        ${userStats.totalWinnings - userStats.totalBetAmount >= 0 ? '+' : ''}$<fmt:formatNumber value="${userStats.totalWinnings - userStats.totalBetAmount}" pattern="#,##0.00"/>
                                    </strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </c:if>

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

        // Filter functionality
        document.getElementById('statusFilter').addEventListener('change', function() {
            const status = this.value.toLowerCase();
            if (status) {
                window.location.href = '${pageContext.request.contextPath}/bets/history?status=' + status;
            } else {
                window.location.href = '${pageContext.request.contextPath}/bets/history';
            }
        });

        document.getElementById('dateFilter').addEventListener('change', function() {
            const date = this.value;
            if (date) {
                filterByDate(date);
            } else {
                showAllRows();
            }
        });

        function filterByDate(period) {
            const rows = document.querySelectorAll('.bet-row');
            const now = new Date();
            let cutoffDate;

            switch(period) {
                case 'today':
                    cutoffDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                    break;
                case 'week':
                    cutoffDate = new Date(now - 7 * 24 * 60 * 60 * 1000);
                    break;
                case 'month':
                    cutoffDate = new Date(now.getFullYear(), now.getMonth(), 1);
                    break;
                default:
                    showAllRows();
                    return;
            }

            rows.forEach(row => {
                // This is a simplified filter - in a real app, you'd parse the actual date
                row.style.display = 'table-row';
            });
        }

        function showAllRows() {
            const rows = document.querySelectorAll('.bet-row');
            rows.forEach(row => {
                row.style.display = 'table-row';
            });
        }

        // Export functionality
        function exportHistory() {
            showAlert('Export functionality coming soon!', 'info');
        }

        // Refresh functionality
        function refreshHistory() {
            location.reload();
        }

        // Cancel bet function
        function cancelBet(betId) {
            if (confirm('Are you sure you want to cancel this bet? You will receive a full refund.')) {
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

        // Initialize performance chart
        const performanceCanvas = document.getElementById('performanceChart');
        if (performanceCanvas) {
            const ctx = performanceCanvas.getContext('2d');

            // Sample performance data
            const dates = ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5'];
            const profits = [0, 50, 30, 150, 120];
            const cumulative = profits.reduce((acc, val, i) => {
                acc.push((acc[i-1] || 0) + val);
                return acc;
            }, []);

            // Draw background grid
            ctx.strokeStyle = 'rgba(160, 163, 189, 0.1)';
            ctx.lineWidth = 1;
            for (let i = 1; i < 5; i++) {
                ctx.beginPath();
                ctx.moveTo(0, (i / 5) * performanceCanvas.height);
                ctx.lineTo(performanceCanvas.width, (i / 5) * performanceCanvas.height);
                ctx.stroke();

                ctx.beginPath();
                ctx.moveTo((i / 5) * performanceCanvas.width, 0);
                ctx.lineTo((i / 5) * performanceCanvas.width, performanceCanvas.height);
                ctx.stroke();
            }

            // Draw profit/loss line
            ctx.strokeStyle = '#6C5CE7';
            ctx.lineWidth = 3;
            ctx.beginPath();

            const maxProfit = Math.max(...cumulative.map(Math.abs));
            const centerY = performanceCanvas.height / 2;

            cumulative.forEach((profit, index) => {
                const x = (index / (cumulative.length - 1)) * performanceCanvas.width;
                const y = centerY - (profit / Math.max(maxProfit, 1)) * (centerY * 0.8);

                if (index === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);

                // Draw points
                ctx.fillStyle = profit >= 0 ? '#00B894' : '#E17055';
                ctx.beginPath();
                ctx.arc(x, y, 4, 0, 2 * Math.PI);
                ctx.fill();
            });
            ctx.stroke();

            // Draw zero line
            ctx.strokeStyle = 'rgba(160, 163, 189, 0.5)';
            ctx.lineWidth = 1;
            ctx.setLineDash([5, 5]);
            ctx.beginPath();
            ctx.moveTo(0, centerY);
            ctx.lineTo(performanceCanvas.width, centerY);
            ctx.stroke();
            ctx.setLineDash([]);

            // Add labels
            ctx.fillStyle = '#FFFFFF';
            ctx.font = '12px Inter';
            dates.forEach((date, index) => {
                const x = (index / (dates.length - 1)) * performanceCanvas.width;
                ctx.fillText(date, x - 20, performanceCanvas.height - 10);
            });
        }

        // Auto-refresh pending bets every 2 minutes
        setInterval(function() {
            const pendingBets = document.querySelectorAll('[data-status="PENDING"]');
            if (pendingBets.length > 0) {
                // Check for bet updates
                console.log('Checking for bet updates...');
            }
        }, 120000);
    </script>
</body>
</html>