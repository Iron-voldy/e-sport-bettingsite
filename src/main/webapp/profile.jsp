<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile - ML Betting</title>
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
                <li><a href="${pageContext.request.contextPath}/profile" class="nav-link active">Profile</a></li>
                <li><a href="${pageContext.request.contextPath}/dashboard?action=logout" class="nav-link">Logout</a></li>
            </ul>
        </div>
    </nav>

    <!-- Profile Header -->
    <section class="container mt-4">
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex align-items-center" style="gap: 2rem;">
                    <div style="position: relative;">
                        <img src="https://via.placeholder.com/100x100/6C5CE7/FFFFFF?text=${sessionScope.user.username.substring(0,1).toUpperCase()}"
                             alt="Profile" style="width: 100px; height: 100px; border-radius: 50%; border: 4px solid var(--primary-color);">
                        <span style="position: absolute; bottom: 0; right: 0; width: 24px; height: 24px; background: var(--success-color); border-radius: 50%; border: 3px solid var(--dark-surface);"></span>
                    </div>

                    <div style="flex: 1;">
                        <h2 style="margin-bottom: 0.5rem;">${sessionScope.user.fullName}</h2>
                        <p style="color: var(--text-secondary); margin-bottom: 0.5rem;">@${sessionScope.user.username}</p>
                        <p style="color: var(--text-muted); margin-bottom: 1rem;">Member since <fmt:formatDate value="${sessionScope.user.createdAt}" pattern="MMM yyyy" /></p>

                        <div class="d-flex" style="gap: 1rem;">
                            <button class="btn btn-primary" data-modal-target="editProfileModal">Edit Profile</button>
                            <button class="btn btn-outline" data-modal-target="changePasswordModal">Change Password</button>
                        </div>
                    </div>

                    <div class="text-right">
                        <div class="stat-card" style="margin: 0;">
                            <span class="stat-value">$<fmt:formatNumber value="${sessionScope.user.walletBalance}" pattern="#,##0.00"/></span>
                            <span class="stat-label">Current Balance</span>
                        </div>
                    </div>
                </div>
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

    <!-- Main Content -->
    <section class="container">
        <div class="d-flex" style="gap: 2rem;">
            <!-- Left Column -->
            <div style="flex: 2;">
                <!-- Betting Statistics -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h3 style="margin: 0;">Betting Statistics</h3>
                    </div>
                    <div class="card-body">
                        <div class="stats-grid">
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

                        <!-- Performance Chart -->
                        <div style="margin-top: 2rem;">
                            <h5>Betting Performance</h5>
                            <canvas id="performanceChart" width="600" height="200"></canvas>
                        </div>
                    </div>
                </div>

                <!-- Recent Betting History -->
                <div class="card">
                    <div class="card-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h3 style="margin: 0;">Recent Betting History</h3>
                            <a href="${pageContext.request.contextPath}/bets/history" class="btn btn-sm btn-outline">View All</a>
                        </div>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty recentBets}">
                                <div class="table-responsive">
                                    <table class="table">
                                        <thead>
                                            <tr>
                                                <th>Date</th>
                                                <th>Match</th>
                                                <th>Team</th>
                                                <th>Amount</th>
                                                <th>Odds</th>
                                                <th>Status</th>
                                                <th>Payout</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${recentBets}" var="bet">
                                                <tr>
                                                    <td>
                                                        <fmt:formatDate value="${bet.betPlacedAt}" pattern="MMM dd, HH:mm" />
                                                    </td>
                                                    <td>
                                                        <div style="font-size: 0.875rem;">
                                                            ${bet.match.team1.teamName} vs ${bet.match.team2.teamName}
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <strong>${bet.selectedTeam.teamName}</strong>
                                                    </td>
                                                    <td>$${bet.betAmount}</td>
                                                    <td>${bet.oddsAtBet}</td>
                                                    <td>
                                                        <span class="badge badge-${bet.status == 'WON' ? 'success' : bet.status == 'LOST' ? 'danger' : 'warning'}">
                                                            ${bet.status}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${bet.status == 'WON'}">
                                                                <span class="text-success">$${bet.potentialWinnings}</span>
                                                            </c:when>
                                                            <c:when test="${bet.status == 'LOST'}">
                                                                <span class="text-danger">-$${bet.betAmount}</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">Pending</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center" style="padding: 2rem;">
                                    <h5>No betting history yet</h5>
                                    <p class="text-secondary">Start betting on matches to see your history here.</p>
                                    <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-primary">
                                        View Available Matches
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Right Column -->
            <div style="flex: 1;">
                <!-- Account Information -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h4 style="margin: 0;">Account Information</h4>
                    </div>
                    <div class="card-body">
                        <div style="display: flex; flex-direction: column; gap: 1rem;">
                            <div>
                                <label style="font-weight: 600; color: var(--text-secondary); font-size: 0.875rem;">EMAIL</label>
                                <div>${sessionScope.user.email}</div>
                            </div>

                            <div>
                                <label style="font-weight: 600; color: var(--text-secondary); font-size: 0.875rem;">PHONE</label>
                                <div>${sessionScope.user.phone != null ? sessionScope.user.phone : 'Not provided'}</div>
                            </div>

                            <div>
                                <label style="font-weight: 600; color: var(--text-secondary); font-size: 0.875rem;">MEMBER SINCE</label>
                                <div><fmt:formatDate value="${sessionScope.user.createdAt}" pattern="MMMM dd, yyyy" /></div>
                            </div>

                            <div>
                                <label style="font-weight: 600; color: var(--text-secondary); font-size: 0.875rem;">ACCOUNT STATUS</label>
                                <div>
                                    <span class="badge badge-success">Active</span>
                                    <span class="badge badge-primary">Verified</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Transactions -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h4 style="margin: 0;">Recent Transactions</h4>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty recentTransactions}">
                                <c:forEach items="${recentTransactions}" var="transaction">
                                    <div style="border-bottom: 1px solid var(--border-color); padding: 1rem 0;">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <div>
                                                <div style="font-weight: 600;">${transaction.transactionType.displayName}</div>
                                                <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                                    <fmt:formatDate value="${transaction.createdAt}" pattern="MMM dd, HH:mm" />
                                                </div>
                                            </div>
                                            <div style="text-align: right;">
                                                <div class="${transaction.credit ? 'text-success' : 'text-danger'}" style="font-weight: 600;">
                                                    ${transaction.credit ? '+' : '-'}$${transaction.amount}
                                                </div>
                                                <div class="badge badge-${transaction.status == 'COMPLETED' ? 'success' : transaction.status == 'FAILED' ? 'danger' : 'warning'}">
                                                    ${transaction.status}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center" style="padding: 1rem;">
                                    <p style="margin: 0; color: var(--text-secondary);">No recent transactions</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Account Settings -->
                <div class="card">
                    <div class="card-header">
                        <h4 style="margin: 0;">Account Settings</h4>
                    </div>
                    <div class="card-body">
                        <div style="display: flex; flex-direction: column; gap: 1rem;">
                            <div class="form-check">
                                <input type="checkbox" id="emailNotifications" class="form-check-input" checked>
                                <label for="emailNotifications" class="form-check-label">Email notifications</label>
                            </div>

                            <div class="form-check">
                                <input type="checkbox" id="smsNotifications" class="form-check-input">
                                <label for="smsNotifications" class="form-check-label">SMS notifications</label>
                            </div>

                            <div class="form-check">
                                <input type="checkbox" id="marketingEmails" class="form-check-input">
                                <label for="marketingEmails" class="form-check-label">Marketing emails</label>
                            </div>

                            <hr style="border-color: var(--border-color);">

                            <div>
                                <label style="font-weight: 600; color: var(--text-secondary); font-size: 0.875rem;">DAILY BET LIMIT</label>
                                <select class="form-control" style="margin-top: 0.5rem;">
                                    <option value="1000">$1,000</option>
                                    <option value="5000">$5,000</option>
                                    <option value="10000" selected>$10,000</option>
                                    <option value="unlimited">Unlimited</option>
                                </select>
                            </div>

                            <button class="btn btn-primary w-100">Save Settings</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Edit Profile Modal -->
    <div id="editProfileModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; align-items: center; justify-content: center;">
        <div class="card" style="width: 100%; max-width: 500px; margin: 2rem;">
            <div class="card-header">
                <h4 style="margin: 0;">Edit Profile</h4>
            </div>
            <div class="card-body">
                <form id="editProfileForm" action="${pageContext.request.contextPath}/dashboard" method="POST">
                    <input type="hidden" name="action" value="updateProfile">

                    <div class="form-group">
                        <label for="editFullName" class="form-label">Full Name</label>
                        <input type="text" id="editFullName" name="fullName" class="form-control"
                               value="${sessionScope.user.fullName}" required>
                    </div>

                    <div class="form-group">
                        <label for="editPhone" class="form-label">Phone Number</label>
                        <input type="tel" id="editPhone" name="phone" class="form-control"
                               value="${sessionScope.user.phone}">
                    </div>

                    <div class="form-group">
                        <label for="editEmail" class="form-label">Email Address</label>
                        <input type="email" id="editEmail" class="form-control"
                               value="${sessionScope.user.email}" disabled>
                        <div class="form-text">Email cannot be changed. Contact support if needed.</div>
                    </div>

                    <div class="d-flex" style="gap: 1rem;">
                        <button type="submit" class="btn btn-primary">Save Changes</button>
                        <button type="button" class="btn btn-outline modal-close">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Change Password Modal -->
    <div id="changePasswordModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; align-items: center; justify-content: center;">
        <div class="card" style="width: 100%; max-width: 400px; margin: 2rem;">
            <div class="card-header">
                <h4 style="margin: 0;">Change Password</h4>
            </div>
            <div class="card-body">
                <form id="changePasswordForm">
                    <div class="form-group">
                        <label for="currentPassword" class="form-label">Current Password</label>
                        <input type="password" id="currentPassword" name="currentPassword" class="form-control" required>
                    </div>

                    <div class="form-group">
                        <label for="newPassword" class="form-label">New Password</label>
                        <input type="password" id="newPassword" name="newPassword" class="form-control" required>
                    </div>

                    <div class="form-group">
                        <label for="confirmNewPassword" class="form-label">Confirm New Password</label>
                        <input type="password" id="confirmNewPassword" name="confirmNewPassword" class="form-control" required>
                    </div>

                    <div class="d-flex" style="gap: 1rem;">
                        <button type="submit" class="btn btn-primary">Change Password</button>
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
        // Initialize performance chart
        const performanceCanvas = document.getElementById('performanceChart');
        if (performanceCanvas) {
            const ctx = performanceCanvas.getContext('2d');

            // Sample performance data
            const dates = ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5'];
            const profits = [50, -20, 120, 80, 200];

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

            const maxProfit = Math.max(...profits.map(Math.abs));
            const centerY = performanceCanvas.height / 2;

            profits.forEach((profit, index) => {
                const x = (index / (profits.length - 1)) * performanceCanvas.width;
                const y = centerY - (profit / maxProfit) * (centerY * 0.8);

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
        }

        // Handle change password form
        document.getElementById('changePasswordForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmNewPassword').value;

            if (newPassword !== confirmPassword) {
                showAlert('New passwords do not match!', 'danger');
                return;
            }

            if (!isPasswordStrong(newPassword)) {
                showAlert('New password must be at least 8 characters with uppercase, lowercase, number and special character.', 'danger');
                return;
            }

            // Simulate password change
            showAlert('Password changed successfully!', 'success');
            closeModal('changePasswordModal');
            this.reset();
        });

        // Handle settings changes
        document.querySelectorAll('.form-check-input').forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                showAlert('Settings updated successfully!', 'success');
            });
        });

        document.querySelector('select.form-control').addEventListener('change', function() {
            showAlert('Betting limit updated successfully!', 'success');
        });
    </script>
</body>
</html>