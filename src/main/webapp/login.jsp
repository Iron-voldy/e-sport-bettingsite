<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - ML Betting</title>
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
                <p style="margin: 0.5rem 0 0 0; color: var(--text-secondary);">Login to your ML Betting account</p>
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
                    <a href="#forgot-password" style="color: var(--primary-color); text-decoration: none;">
                        Forgot your password?
                    </a>
                </div>
            </div>

            <div class="card-footer text-center">
                <p style="margin: 0; color: var(--text-secondary);">
                    Don't have an account?
                    <a href="${pageContext.request.contextPath}/register" style="color: var(--primary-color); font-weight: 600; text-decoration: none;">
                        Sign up here
                    </a>
                </p>
            </div>
        </div>

        <!-- Demo Credentials Info -->
        <div class="card mt-3" style="background: rgba(108, 92, 231, 0.1); border-color: var(--primary-color);">
            <div class="card-body text-center">
                <h5 style="color: var(--primary-color); margin-bottom: 1rem;">Demo Account</h5>
                <p style="margin-bottom: 0.5rem; color: var(--text-secondary);">
                    <strong>Email:</strong> demo@mlbetting.com<br>
                    <strong>Password:</strong> Demo123!@#
                </p>
                <p style="margin: 0; font-size: 0.875rem; color: var(--text-muted);">
                    Use these credentials to explore the platform
                </p>
            </div>
        </div>
    </section>

    <!-- Features Section -->
    <section class="container mt-5">
        <h3 class="text-center mb-4">Why Choose ML Betting?</h3>
        <div class="stats-grid">
            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-success mb-3" style="width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 20px;">🔒</span>
                    </div>
                    <h5>Secure & Safe</h5>
                    <p style="margin: 0; color: var(--text-secondary);">Your funds and data are protected with bank-level security</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-warning mb-3" style="width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 20px;">⚡</span>
                    </div>
                    <h5>Instant Payouts</h5>
                    <p style="margin: 0; color: var(--text-secondary);">Get your winnings instantly after match results</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-primary mb-3" style="width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 20px;">📱</span>
                    </div>
                    <h5>Mobile Friendly</h5>
                    <p style="margin: 0; color: var(--text-secondary);">Bet on the go with our responsive mobile platform</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body text-center">
                    <div class="feature-icon bg-secondary mb-3" style="width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
                        <span style="font-size: 20px;">🎮</span>
                    </div>
                    <h5>Live Betting</h5>
                    <p style="margin: 0; color: var(--text-secondary);">Bet during live matches with real-time odds</p>
                </div>
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
                </div>

                <div class="footer-section">
                    <h5>Quick Links</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/">Home</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming">Upcoming Matches</a>
                        <a href="${pageContext.request.contextPath}/register">Create Account</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Support</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="#help">Help Center</a>
                        <a href="#contact">Contact Us</a>
                        <a href="#terms">Terms & Conditions</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Security</h5>
                    <p>Your account is protected with 256-bit SSL encryption and two-factor authentication options.</p>
                    <div style="margin-top: 1rem;">
                        <span class="badge badge-success">SSL Secured</span>
                        <span class="badge badge-primary">Encrypted</span>
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

    <!-- Forgot Password Modal -->
    <div id="forgotPasswordModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; align-items: center; justify-content: center;">
        <div class="card" style="width: 100%; max-width: 400px; margin: 2rem;">
            <div class="card-header">
                <h4 style="margin: 0;">Reset Password</h4>
            </div>
            <div class="card-body">
                <p style="color: var(--text-secondary);">Enter your email address and we'll send you a link to reset your password.</p>
                <form id="forgotPasswordForm">
                    <div class="form-group">
                        <label for="resetEmail" class="form-label">Email Address</label>
                        <input type="email" id="resetEmail" name="resetEmail" class="form-control" required>
                    </div>
                    <div class="d-flex" style="gap: 1rem;">
                        <button type="submit" class="btn btn-primary">Send Reset Link</button>
                        <button type="button" class="btn btn-outline modal-close">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        // Handle forgot password link
        document.querySelector('a[href="#forgot-password"]').addEventListener('click', function(e) {
            e.preventDefault();
            openModal('forgotPasswordModal');
        });

        // Handle forgot password form
        document.getElementById('forgotPasswordForm').addEventListener('submit', function(e) {
            e.preventDefault();
            showAlert('Password reset link has been sent to your email!', 'success');
            closeModal('forgotPasswordModal');
        });
    </script>
</body>
</html>