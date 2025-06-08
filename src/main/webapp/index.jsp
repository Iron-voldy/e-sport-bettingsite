<%-- Fixed index.jsp with proper CSS and image references --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>E-Sports Betting - Mobile Legends Championship</title>

    <!-- Fixed CSS Loading -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">

    <style>
        /* Emergency inline styles in case external CSS doesn't load */
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #0F0F23 0%, #1A1A2E 100%);
            color: #FFFFFF;
            margin: 0;
            padding: 0;
        }
        .navbar {
            background: rgba(15, 15, 35, 0.95);
            padding: 1rem 0;
            border-bottom: 1px solid #2A2D5A;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        .navbar .container {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .navbar-brand {
            color: #6C5CE7;
            text-decoration: none;
            font-size: 1.5rem;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .navbar-nav {
            display: flex;
            list-style: none;
            gap: 2rem;
            align-items: center;
            margin: 0;
            padding: 0;
        }
        .nav-link {
            color: #A0A3BD;
            text-decoration: none;
            padding: 0.5rem 1rem;
            border-radius: 8px;
            transition: all 0.3s ease;
        }
        .nav-link:hover, .nav-link.active {
            color: #FFFFFF;
            background: rgba(108, 92, 231, 0.1);
        }
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 12px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-block;
            text-align: center;
        }
        .btn-primary {
            background: linear-gradient(135deg, #6C5CE7, #5A4FCF);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(108, 92, 231, 0.3);
        }
        .hero {
            padding: 4rem 0;
            text-align: center;
        }
        .hero-title {
            font-size: 3.5rem;
            font-weight: 700;
            margin-bottom: 1rem;
            background: linear-gradient(135deg, #6C5CE7, #00D2D3);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .hero-subtitle {
            font-size: 1.25rem;
            color: #A0A3BD;
            margin-bottom: 2rem;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar">
        <div class="container">
            <a href="${pageContext.request.contextPath}/" class="navbar-brand">
                <!-- Using Bootstrap Icon instead of placeholder -->
                <i class="bi bi-controller" style="font-size: 2rem;"></i>
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
            <div style="display: flex; justify-content: center; gap: 1rem;">
                <c:choose>
                    <c:when test="${sessionScope.user != null}">
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">Go to Dashboard</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline">View Matches</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Start Betting</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn btn-outline">View Matches</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <!-- System Status (for debugging) -->
    <c:if test="${serviceStatus == 'unavailable' || dbStatus == 'unhealthy'}">
        <section class="container" style="margin-top: 2rem;">
            <div style="background: rgba(225, 112, 85, 0.1); border: 1px solid #E17055; border-radius: 8px; padding: 1rem; text-align: center;">
                <p style="margin: 0; color: #E17055;">
                    <i class="bi bi-exclamation-triangle"></i>
                    System Status: Services are initializing. Some features may be limited.
                    <br>Service: ${serviceStatus} | Database: ${dbStatus}
                    <br><a href="${pageContext.request.contextPath}/test/database" style="color: #E17055;">View Database Test</a>
                </p>
            </div>
        </section>
    </c:if>

    <!-- Featured Matches -->
    <section class="container" style="margin-top: 3rem;">
        <h2 style="text-align: center; margin-bottom: 2rem;">Featured Matches</h2>

        <c:choose>
            <c:when test="${not empty featuredMatches}">
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 1.5rem;">
                    <c:forEach items="${featuredMatches}" var="match" varStatus="status">
                        <c:if test="${status.index < 3}">
                            <div style="background: #1A1A2E; border: 1px solid #2A2D5A; border-radius: 16px; padding: 1.5rem; transition: all 0.3s ease;">
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                                    <span style="padding: 4px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; background: #FDCB6E; color: #0F0F23;">
                                        ${match.status}
                                    </span>
                                    <span style="color: #A0A3BD; font-size: 0.875rem;">
                                        <fmt:formatDate value="${match.matchDate}" pattern="MMM dd, HH:mm" />
                                    </span>
                                </div>

                                <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem;">
                                    <div style="flex: 1; text-align: center;">
                                        <!-- Team 1 Icon -->
                                        <div style="width: 60px; height: 60px; border-radius: 50%; background: linear-gradient(135deg, #FF6B6B, #FF5252); display: flex; align-items: center; justify-content: center; margin: 0 auto 0.5rem; color: white; font-weight: bold; font-size: 1.2rem;">
                                            ${match.team1.teamCode}
                                        </div>
                                        <div style="font-weight: 600; margin-bottom: 0.5rem;">${match.team1.teamName}</div>
                                        <div style="font-size: 1.2rem; font-weight: 700; color: #6C5CE7;">${match.team1Odds}</div>
                                    </div>

                                    <div style="text-align: center; font-weight: 700; color: #6C7293; font-size: 1.2rem;">VS</div>

                                    <div style="flex: 1; text-align: center;">
                                        <!-- Team 2 Icon -->
                                        <div style="width: 60px; height: 60px; border-radius: 50%; background: linear-gradient(135deg, #00D2D3, #00B2D3); display: flex; align-items: center; justify-content: center; margin: 0 auto 0.5rem; color: white; font-weight: bold; font-size: 1.2rem;">
                                            ${match.team2.teamCode}
                                        </div>
                                        <div style="font-weight: 600; margin-bottom: 0.5rem;">${match.team2.teamName}</div>
                                        <div style="font-size: 1.2rem; font-weight: 700; color: #6C5CE7;">${match.team2Odds}</div>
                                    </div>
                                </div>

                                <div style="display: flex; justify-content: space-between; align-items: center;">
                                    <span style="color: #A0A3BD; font-size: 0.875rem;">${match.tournament.tournamentName}</span>
                                    <a href="${pageContext.request.contextPath}/matches/details/${match.id}" class="btn btn-primary" style="font-size: 0.875rem; padding: 8px 16px;">
                                        View Details
                                    </a>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div style="text-align: center; padding: 4rem 2rem;">
                    <i class="bi bi-calendar-x" style="font-size: 4rem; color: #6C7293; margin-bottom: 1rem;"></i>
                    <h3>No matches available</h3>
                    <p style="color: #A0A3BD;">New matches will be posted soon. Check back later!</p>
                    <c:if test="${not empty errorMessage}">
                        <div style="background: rgba(225, 112, 85, 0.1); padding: 1rem; border-radius: 8px; margin-top: 1rem;">
                            <p style="color: #E17055; margin: 0;">${errorMessage}</p>
                        </div>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>

        <div style="text-align: center; margin-top: 2rem;">
            <a href="${pageContext.request.contextPath}/matches/upcoming" class="btn" style="background: linear-gradient(135deg, #00D2D3, #00B2D3); color: white;">
                View All Matches
            </a>
        </div>
    </section>

    <!-- Platform Stats -->
    <section class="container" style="margin: 4rem auto;">
        <h2 style="text-align: center; margin-bottom: 2rem;">Platform Statistics</h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem;">
            <div style="background: linear-gradient(135deg, #1A1A2E, #2A2D5A); border: 1px solid #2A2D5A; border-radius: 12px; padding: 1.5rem; text-align: center;">
                <span style="display: block; font-size: 2.5rem; font-weight: 700; color: #6C5CE7; margin-bottom: 0.5rem;">10,000+</span>
                <span style="color: #A0A3BD; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 1px;">Active Users</span>
            </div>
            <div style="background: linear-gradient(135deg, #1A1A2E, #2A2D5A); border: 1px solid #2A2D5A; border-radius: 12px; padding: 1.5rem; text-align: center;">
                <span style="display: block; font-size: 2.5rem; font-weight: 700; color: #6C5CE7; margin-bottom: 0.5rem;">$2.5M</span>
                <span style="color: #A0A3BD; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 1px;">Total Bets Placed</span>
            </div>
            <div style="background: linear-gradient(135deg, #1A1A2E, #2A2D5A); border: 1px solid #2A2D5A; border-radius: 12px; padding: 1.5rem; text-align: center;">
                <span style="display: block; font-size: 2.5rem; font-weight: 700; color: #6C5CE7; margin-bottom: 0.5rem;">150+</span>
                <span style="color: #A0A3BD; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 1px;">Matches This Month</span>
            </div>
            <div style="background: linear-gradient(135deg, #1A1A2E, #2A2D5A); border: 1px solid #2A2D5A; border-radius: 12px; padding: 1.5rem; text-align: center;">
                <span style="display: block; font-size: 2.5rem; font-weight: 700; color: #6C5CE7; margin-bottom: 0.5rem;">98.5%</span>
                <span style="color: #A0A3BD; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 1px;">Payout Rate</span>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer style="background: #1A1A2E; border-top: 1px solid #2A2D5A; padding: 3rem 0 1rem; margin-top: 4rem;">
        <div class="container">
            <div style="text-align: center; padding-top: 2rem; border-top: 1px solid #2A2D5A; color: #A0A3BD;">
                <p>&copy; 2024 ML Betting Platform. All rights reserved. |
                   <span style="color: #FDCB6E;">Please bet responsibly. 18+ only.</span>
                </p>
            </div>
        </div>
    </footer>

    <!-- JavaScript -->
    <script src="${pageContext.request.contextPath}/js/script.js"></script>

    <!-- Emergency JavaScript in case external script fails -->
    <script>
        // Basic functionality if external script doesn't load
        console.log('E-Sports Betting Platform loaded');

        // Display alerts if any
        <c:if test="${not empty successMessage}">
            alert('${successMessage}');
        </c:if>

        <c:if test="${not empty errorMessage}">
            alert('${errorMessage}');
        </c:if>
    </script>
</body>
</html>