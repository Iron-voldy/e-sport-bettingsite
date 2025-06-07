<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>E-Sports Betting - Mobile Legends Championship</title>
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
                <li><a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a></li>
                <c:choose>
                    <c:when test="${sessionScope.user != null}">
                        <li><a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a></li>
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
            <h1 class="hero-title">Mobile Legends Championship Betting</h1>
            <p class="hero-subtitle">
                Experience the thrill of competitive gaming with real-time betting on Mobile Legends Professional League matches.
                Join thousands of fans in the ultimate e-sports betting platform.
            </p>
            <div class="d-flex justify-content-center" style="gap: 1rem;">
                <c:choose>
                    <c:when test="${sessionScope.user != null}">
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary btn-lg">Go to Dashboard</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline btn-lg">View Matches</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-lg">Start Betting</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline btn-lg">View Matches</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <!-- Featured Matches -->
    <section class="container mt-5">
        <h2 class="text-center mb-4">Featured Matches</h2>

        <c:if test="${not empty featuredMatches}">
            <div class="stats-grid">
                <c:forEach items="${featuredMatches}" var="match" varStatus="status">
                    <c:if test="${status.index < 3}">
                        <div class="match-card" data-match-id="${match.id}">
                            <div class="match-header">
                                <span class="match-status ${match.status == 'LIVE' ? 'status-live' : 'status-upcoming'}">
                                    ${match.status}
                                </span>
                                <span class="match-date">
                                    <fmt:formatDate value="${match.matchDate}" pattern="MMM dd, HH:mm" />
                                </span>
                            </div>

                            <div class="teams-container">
                                <div class="team">
                                    <img src="https://via.placeholder.com/60x60/FF6B6B/FFFFFF?text=${match.team1.teamCode}"
                                         alt="${match.team1.teamName}" class="team-logo">
                                    <div class="team-name">${match.team1.teamName}</div>
                                    <div class="team-odds">${match.team1Odds}</div>
                                </div>

                                <div class="vs-divider">VS</div>

                                <div class="team">
                                    <img src="https://via.placeholder.com/60x60/00D2D3/FFFFFF?text=${match.team2.teamCode}"
                                         alt="${match.team2.teamName}" class="team-logo">
                                    <div class="team-name">${match.team2.teamName}</div>
                                    <div class="team-odds">${match.team2Odds}</div>
                                </div>
                            </div>

                            <div class="match-info">
                                <span class="tournament-name">${match.tournament.tournamentName}</span>
                                <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-primary btn-sm">
                                    View Details
                                </a>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </c:if>

        <div class="text-center mt-4">
            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-secondary">
                View All Matches
            </a>
        </div>
    </section>

    <!-- Platform Stats -->
    <section class="container mt-5">
        <h2 class="text-center mb-4">Platform Statistics</h2>
        <div class="stats-grid">
            <div class="stat-card">
                <span class="stat-value">10,000+</span>
                <span class="stat-label">Active Users</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">$2.5M</span>
                <span class="stat-label">Total Bets Placed</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">150+</span>
                <span class="stat-label">Matches This Month</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">98.5%</span>
                <span class="stat-label">Payout Rate</span>
            </div>
        </div>
    </section>

    <!-- How It Works -->
    <section class="container mt-5">
        <h2 class="text-center mb-4">How It Works</h2>
        <div class="stats-grid">
            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-primary mb-3" style="width: 60px; height: 60px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 24px;">1</span>
                    </div>
                    <h4>Sign Up</h4>
                    <p>Create your account and get a welcome bonus to start betting on your favorite teams.</p>
                </div>
            </div>
            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-secondary mb-3" style="width: 60px; height: 60px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 24px;">2</span>
                    </div>
                    <h4>Choose Match</h4>
                    <p>Browse upcoming matches and select the teams you think will win in the championship.</p>
                </div>
            </div>
            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-success mb-3" style="width: 60px; height: 60px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 24px;">3</span>
                    </div>
                    <h4>Place Bet</h4>
                    <p>Place your bets with competitive odds and watch the matches live to see if you win.</p>
                </div>
            </div>
            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-warning mb-3" style="width: 60px; height: 60px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 24px;">4</span>
                    </div>
                    <h4>Win Big</h4>
                    <p>Collect your winnings instantly when your predictions are correct. Withdraw anytime!</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Recent Winners -->
    <c:if test="${sessionScope.user != null}">
        <section class="container mt-5">
            <h2 class="text-center mb-4">Recent Winners</h2>
            <div class="card">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Match</th>
                                    <th>Bet Amount</th>
                                    <th>Winnings</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>User****123</td>
                                    <td>RRQ vs EVOS</td>
                                    <td>$50.00</td>
                                    <td class="text-success">$95.00</td>
                                    <td>2 hours ago</td>
                                </tr>
                                <tr>
                                    <td>Player****456</td>
                                    <td>Blacklist vs ONIC</td>
                                    <td>$25.00</td>
                                    <td class="text-success">$62.50</td>
                                    <td>5 hours ago</td>
                                </tr>
                                <tr>
                                    <td>Gamer****789</td>
                                    <td>Echo vs BTK</td>
                                    <td>$100.00</td>
                                    <td class="text-success">$180.00</td>
                                    <td>1 day ago</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </section>
    </c:if>

    <!-- Live Matches Ticker -->
    <section class="mt-5" style="background: var(--dark-surface); padding: 1rem 0; border-top: 1px solid var(--border-color); border-bottom: 1px solid var(--border-color);">
        <div class="container">
            <div class="d-flex justify-content-between align-items-center">
                <span class="text-primary font-weight-bold">🔴 LIVE NOW:</span>
                <div class="d-flex" style="gap: 2rem; overflow-x: auto;">
                    <span>RRQ Hoshi vs EVOS Legends - Game 2</span>
                    <span>•</span>
                    <span>Blacklist vs ONIC - Game 1</span>
                    <span>•</span>
                    <span>Echo vs Team Flash - Starting Soon</span>
                </div>
                <a href="${pageContext.request.contextPath}/matches/live" class="btn btn-sm btn-danger">Watch Live</a>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
        <div class="container">
            <div class="footer-content">
                <div class="footer-section">
                    <h5>About ML Betting</h5>
                    <p>The premier platform for Mobile Legends Professional League betting. Experience the excitement of e-sports with secure, fair, and transparent betting.</p>
                    <div style="margin-top: 1rem;">
                        <span class="badge badge-success">Licensed</span>
                        <span class="badge badge-primary">Secure</span>
                        <span class="badge badge-warning">24/7 Support</span>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Quick Links</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/">Home</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming">Upcoming Matches</a>
                        <a href="${pageContext.request.contextPath}/matches/live">Live Matches</a>
                        <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                        <a href="${pageContext.request.contextPath}/results">Results</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Support</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="#help">Help Center</a>
                        <a href="#contact">Contact Us</a>
                        <a href="#responsible">Responsible Gaming</a>
                        <a href="#terms">Terms & Conditions</a>
                        <a href="#privacy">Privacy Policy</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Connect With Us</h5>
                    <p>Follow us for the latest updates on matches, odds, and exclusive promotions.</p>
                    <div style="display: flex; gap: 1rem; margin-top: 1rem;">
                        <a href="#facebook" class="btn btn-outline btn-sm">Facebook</a>
                        <a href="#twitter" class="btn btn-outline btn-sm">Twitter</a>
                        <a href="#discord" class="btn btn-outline btn-sm">Discord</a>
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

    <!-- Display alerts if any -->
    <c:if test="${not empty successMessage}">
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                showAlert('${successMessage}', 'success');
            });
        </script>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                showAlert('${errorMessage}', 'danger');
            });
        </script>
    </c:if>
</body>
</html>