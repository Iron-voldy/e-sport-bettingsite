<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - ML Betting</title>

    <!-- Fixed CSS Loading -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">

    <!-- Emergency inline styles in case external CSS doesn't load -->
    <style>
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
        .card {
            background: #1A1A2E;
            border: 1px solid #2A2D5A;
            border-radius: 16px;
            overflow: hidden;
        }
        .card-header {
            padding: 1.5rem;
            border-bottom: 1px solid #2A2D5A;
            background: rgba(108, 92, 231, 0.05);
        }
        .card-body {
            padding: 1.5rem;
        }
        .card-footer {
            padding: 1rem 1.5rem;
            border-top: 1px solid #2A2D5A;
            background: rgba(0, 0, 0, 0.1);
        }
        .form-group {
            margin-bottom: 1.5rem;
        }
        .form-label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #FFFFFF;
        }
        .form-control {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #2A2D5A;
            border-radius: 8px;
            background: #2A2D5A;
            color: #FFFFFF;
            font-size: 1rem;
            transition: all 0.3s ease;
            font-family: inherit;
            box-sizing: border-box;
        }
        .form-control:focus {
            outline: none;
            border-color: #6C5CE7;
            box-shadow: 0 0 0 3px rgba(108, 92, 231, 0.1);
        }
        .form-check {
            display: flex;
            align-items: center;
            margin-bottom: 1rem;
        }
        .form-check-input {
            margin-right: 0.5rem;
            width: 18px;
            height: 18px;
        }
        .form-check-label {
            color: #A0A3BD;
            cursor: pointer;
        }
        .alert {
            padding: 1rem 1.5rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            border: 1px solid transparent;
        }
        .alert-danger {
            background: rgba(225, 112, 85, 0.1);
            color: #E17055;
            border-color: #E17055;
        }
        .alert-info {
            background: rgba(116, 185, 255, 0.1);
            color: #74B9FF;
            border-color: #74B9FF;
        }
        .text-center { text-align: center; }
        .mt-3 { margin-top: 1rem; }
        .w-100 { width: 100%; }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar">
        <div class="container">
            <a href="${pageContext.request.contextPath}/" class="navbar-brand">
                <i class="bi bi-controller" style="font-size: 2rem;"></i>
                ML Betting
            </a>

            <ul class="navbar-nav">
                <li><a href="${pageContext.request.contextPath}/" class="nav-link">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/matches/upcoming" class="nav-link">Matches</a></li>
                <li><a href="${pageContext.request.contextPath}/login" class="nav-link active">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Sign Up</a></li>
            </ul>
        </div>
    </nav>

    <!-- Login Form -->
    <section class="container" style="max-width: 500px; margin-top: 4rem; margin-bottom: 4rem;">
        <div class="card">
            <div class="card-header text-center">
                <h2 style="margin: 0;">Welcome Back</h2>
                <p style="margin: 0.5rem 0 0 0; color: #A0A3BD;">Login to your ML Betting account</p>
            </div>

            <div class="card-body">
                <!-- Display error message if any -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger">
                        <span>${errorMessage}</span>
                    </div>
                </c:if>

                <form id="loginForm" action="${pageContext.request.contextPath}/login" method="POST">
                    <div class="form-group">
                        <label for="email" class="form-label">Email Address</label>
                        <input type="email" id="email" name="email" class="form-control"
                               placeholder="Enter your email address" required
                               value="${email != null ? email : ''}">
                    </div>

                    <div class="form-group">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" id="password" name="password" class="form-control"
                               placeholder="Enter your password" required>
                    </div>

                    <div class="form-check">
                        <input type="checkbox" id="rememberMe" name="rememberMe" class="form-check-input">
                        <label for="rememberMe" class="form-check-label">Remember me for 7 days</label>
                    </div>

                    <button type="submit" class="btn btn-primary w-100">
                        Login to Account
                    </button>
                </form>

                <div class="text-center mt-3">
                    <a href="#forgot-password" style="color: #6C5CE7; text-decoration: none;">
                        Forgot your password?
                    </a>
                </div>
            </div>

            <div class="card-footer text-center">
                <p style="margin: 0; color: #A0A3BD;">
                    Don't have an account?
                    <a href="${pageContext.request.contextPath}/register" style="color: #6C5CE7; font-weight: 600; text-decoration: none;">
                        Sign up here
                    </a>
                </p>
            </div>
        </div>

        <!-- Demo Credentials Info -->
        <div class="card mt-3" style="background: rgba(108, 92, 231, 0.1); border-color: #6C5CE7;">
            <div class="card-body text-center">
                <h5 style="color: #6C5CE7; margin-bottom: 1rem;">Demo Account</h5>
                <p style="margin-bottom: 0.5rem; color: #A0A3BD;">
                    <strong>Email:</strong> demo@mlbetting.com<br>
                    <strong>Password:</strong> Demo123!@#
                </p>
                <p style="margin: 0; font-size: 0.875rem; color: #6C7293;">
                    Use these credentials to explore the platform
                </p>
            </div>
        </div>
    </section>

    <!-- Features Section -->
    <section class="container" style="margin-top: 3rem;">
        <h3 style="text-align: center; margin-bottom: 2rem;">Why Choose ML Betting?</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem;">
            <div class="card">
                <div class="card-body text-center">
                    <div style="width: 50px; height: 50px; border-radius: 50%; background: #00B894; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                        <span style="font-size: 20px;">🔒</span>
                    </div>
                    <h5>Secure & Safe</h5>
                    <p style="margin: 0; color: #A0A3BD;">Your funds and data are protected with bank-level security</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body text-center">
                    <div style="width: 50px; height: 50px; border-radius: 50%; background: #FDCB6E; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                        <span style="font-size: 20px;">⚡</span>
                    </div>
                    <h5>Instant Payouts</h5>
                    <p style="margin: 0; color: #A0A3BD;">Get your winnings instantly after match results</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body text-center">
                    <div style="width: 50px; height: 50px; border-radius: 50%; background: #6C5CE7; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                        <span style="font-size: 20px;">📱</span>
                    </div>
                    <h5>Mobile Friendly</h5>
                    <p style="margin: 0; color: #A0A3BD;">Bet on the go with our responsive mobile platform</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body text-center">
                    <div style="width: 50px; height: 50px; border-radius: 50%; background: #00D2D3; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                        <span style="font-size: 20px;">🎮</span>
                    </div>
                    <h5>Live Betting</h5>
                    <p style="margin: 0; color: #A0A3BD;">Bet during live matches with real-time odds</p>
                </div>
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
        // Basic login form handling if external script doesn't load
        document.getElementById('loginForm').addEventListener('submit', function(e) {
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            if (!email || !password) {
                e.preventDefault();
                alert('Please enter both email and password');
                return false;
            }
        });

        // Forgot password link
        document.querySelector('a[href="#forgot-password"]').addEventListener('click', function(e) {
            e.preventDefault();
            alert('Password reset functionality will be available soon. Please contact support if needed.');
        });

        // Display alerts if any
        <c:if test="${not empty successMessage}">
            alert('${successMessage}');
        </c:if>

        <c:if test="${not empty errorMessage}">
            console.log('Error: ${errorMessage}');
        </c:if>
    </script>
</body>
</html>