<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up - ML Betting</title>
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
                <li><a href="${pageContext.request.contextPath}/login" class="nav-link">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register" class="nav-link active">Sign Up</a></li>
            </ul>
        </div>
    </nav>

    <!-- Registration Form -->
    <section class="container" style="max-width: 600px; margin-top: 4rem; margin-bottom: 4rem;">
        <div class="card">
            <div class="card-header text-center">
                <h2 style="margin: 0;">Create Your Account</h2>
                <p style="margin: 0.5rem 0 0 0; color: var(--text-secondary);">Join thousands of Mobile Legends fans</p>
            </div>

            <div class="card-body">
                <!-- Display error message if any -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger">
                        <span>${errorMessage}</span>
                    </div>
                </c:if>

                <!-- Welcome Bonus Alert -->
                <div class="alert alert-success">
                    <span>🎉 <strong>Welcome Bonus:</strong> Get $100 free when you create your account!</span>
                </div>

                <form id="registerForm" action="${pageContext.request.contextPath}/register" method="POST">
                    <div class="form-group">
                        <label for="fullName" class="form-label">Full Name *</label>
                        <input type="text" id="fullName" name="fullName" class="form-control"
                               placeholder="Enter your full name" required
                               value="${fullName != null ? fullName : ''}">
                    </div>

                    <div class="form-group">
                        <label for="username" class="form-label">Username *</label>
                        <input type="text" id="username" name="username" class="form-control"
                               placeholder="Choose a unique username" required
                               value="${username != null ? username : ''}">
                        <div class="form-text">3-20 characters, letters, numbers and underscores only</div>
                    </div>

                    <div class="form-group">
                        <label for="email" class="form-label">Email Address *</label>
                        <input type="email" id="email" name="email" class="form-control"
                               placeholder="Enter your email address" required
                               value="${email != null ? email : ''}">
                    </div>

                    <div class="form-group">
                        <label for="phone" class="form-label">Phone Number</label>
                        <input type="tel" id="phone" name="phone" class="form-control"
                               placeholder="Enter your phone number (optional)"
                               value="${phone != null ? phone : ''}">
                    </div>

                    <div class="form-group">
                        <label for="password" class="form-label">Password *</label>
                        <input type="password" id="password" name="password" class="form-control"
                               placeholder="Create a strong password" required>
                        <div class="form-text">Minimum 8 characters with uppercase, lowercase, number and special character</div>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword" class="form-label">Confirm Password *</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                               placeholder="Confirm your password" required>
                    </div>

                    <!-- Password Strength Indicator -->
                    <div class="form-group">
                        <label class="form-label">Password Strength</label>
                        <div class="progress">
                            <div id="passwordStrength" class="progress-bar" style="width: 0%;"></div>
                        </div>
                        <div id="passwordStrengthText" class="form-text">Enter a password to see strength</div>
                    </div>

                    <div class="form-check">
                        <input type="checkbox" id="terms" name="terms" class="form-check-input" required>
                        <label for="terms" class="form-check-label">
                            I agree to the <a href="#terms" style="color: var(--primary-color);">Terms & Conditions</a>
                            and <a href="#privacy" style="color: var(--primary-color);">Privacy Policy</a> *
                        </label>
                    </div>

                    <div class="form-check">
                        <input type="checkbox" id="newsletter" name="newsletter" class="form-check-input">
                        <label for="newsletter" class="form-check-label">
                            Subscribe to newsletter for match updates and exclusive promotions
                        </label>
                    </div>

                    <div class="form-check">
                        <input type="checkbox" id="ageConfirm" name="ageConfirm" class="form-check-input" required>
                        <label for="ageConfirm" class="form-check-label">
                            I confirm that I am 18 years or older *
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primary w-100">
                        Create Account & Get $100 Bonus
                    </button>
                </form>
            </div>

            <div class="card-footer text-center">
                <p style="margin: 0; color: var(--text-secondary);">
                    Already have an account?
                    <a href="${pageContext.request.contextPath}/login" style="color: var(--primary-color); font-weight: 600; text-decoration: none;">
                        Login here
                    </a>
                </p>
            </div>
        </div>
    </section>

    <!-- Benefits Section -->
    <section class="container mt-5">
        <h3 class="text-center mb-4">Join the Community</h3>
        <div class="stats-grid">
            <div class="stat-card">
                <span class="stat-value">$100</span>
                <span class="stat-label">Welcome Bonus</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">24/7</span>
                <span class="stat-label">Live Betting</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">Instant</span>
                <span class="stat-label">Withdrawals</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">98.5%</span>
                <span class="stat-label">Payout Rate</span>
            </div>
        </div>
    </section>

    <!-- Social Proof -->
    <section class="container mt-5">
        <div class="card" style="background: linear-gradient(135deg, var(--primary-color), var(--secondary-color)); color: white;">
            <div class="card-body text-center">
                <h4 style="margin-bottom: 1rem;">Join 10,000+ Happy Bettors</h4>
                <div class="d-flex justify-content-center" style="gap: 2rem; flex-wrap: wrap;">
                    <div style="text-align: center;">
                        <div style="font-size: 2rem; font-weight: 700;">⭐⭐⭐⭐⭐</div>
                        <div style="font-size: 0.9rem;">4.8/5 Rating</div>
                    </div>
                    <div style="text-align: center;">
                        <div style="font-size: 2rem; font-weight: 700;">🏆</div>
                        <div style="font-size: 0.9rem;">Best E-Sports Platform</div>
                    </div>
                    <div style="text-align: center;">
                        <div style="font-size: 2rem; font-weight: 700;">🔒</div>
                        <div style="font-size: 0.9rem;">Bank-Level Security</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Testimonials -->
    <section class="container mt-5">
        <h3 class="text-center mb-4">What Our Users Say</h3>
        <div class="stats-grid">
            <div class="card">
                <div class="card-body">
                    <div style="color: var(--warning-color); margin-bottom: 1rem;">⭐⭐⭐⭐⭐</div>
                    <p style="margin-bottom: 1rem; color: var(--text-secondary);">
                        "Amazing platform! The live betting feature is incredible and payouts are always instant.
                        Won $500 last week on the MPL finals!"
                    </p>
                    <div style="font-weight: 600; color: var(--text-primary);">- GameMaster2024</div>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <div style="color: var(--warning-color); margin-bottom: 1rem;">⭐⭐⭐⭐⭐</div>
                    <p style="margin-bottom: 1rem; color: var(--text-secondary);">
                        "Best odds in the market and the mobile app works perfectly. Customer support is also very helpful.
                        Highly recommended!"
                    </p>
                    <div style="font-weight: 600; color: var(--text-primary);">- ProGamer_PH</div>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <div style="color: var(--warning-color); margin-bottom: 1rem;">⭐⭐⭐⭐⭐</div>
                    <p style="margin-bottom: 1rem; color: var(--text-secondary);">
                        "The welcome bonus really helped me get started. Interface is clean and easy to use.
                        Perfect for Mobile Legends betting!"
                    </p>
                    <div style="font-weight: 600; color: var(--text-primary);">- EsportsEnthusiast</div>
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
                    <h5>Getting Started</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/">Home</a>
                        <a href="${pageContext.request.contextPath}/matches/upcoming">Browse Matches</a>
                        <a href="${pageContext.request.contextPath}/login">Login</a>
                        <a href="#help">How to Bet</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Support</h5>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <a href="#help">Help Center</a>
                        <a href="#contact">24/7 Live Chat</a>
                        <a href="#responsible">Responsible Gaming</a>
                        <a href="#terms">Terms & Conditions</a>
                    </div>
                </div>

                <div class="footer-section">
                    <h5>Security & Trust</h5>
                    <p>Your data and funds are protected with industry-standard security measures.</p>
                    <div style="margin-top: 1rem;">
                        <span class="badge badge-success">SSL Secured</span>
                        <span class="badge badge-primary">Licensed</span>
                        <span class="badge badge-warning">Audited</span>
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
        // Password strength checker
        document.getElementById('password').addEventListener('input', function() {
            const password = this.value;
            const strengthBar = document.getElementById('passwordStrength');
            const strengthText = document.getElementById('passwordStrengthText');

            let score = 0;
            let feedback = [];

            // Length check
            if (password.length >= 8) score += 25;
            else feedback.push('at least 8 characters');

            // Uppercase check
            if (/[A-Z]/.test(password)) score += 25;
            else feedback.push('uppercase letter');

            // Lowercase check
            if (/[a-z]/.test(password)) score += 25;
            else feedback.push('lowercase letter');

            // Number check
            if (/\d/.test(password)) score += 25;
            else feedback.push('number');

            // Special character check
            if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 25;
            else feedback.push('special character');

            // Update progress bar
            strengthBar.style.width = Math.min(score, 100) + '%';

            // Update color and text
            if (score === 0) {
                strengthBar.style.background = 'var(--text-muted)';
                strengthText.textContent = 'Enter a password to see strength';
                strengthText.style.color = 'var(--text-muted)';
            } else if (score < 50) {
                strengthBar.style.background = 'var(--danger-color)';
                strengthText.textContent = 'Weak - Add: ' + feedback.join(', ');
                strengthText.style.color = 'var(--danger-color)';
            } else if (score < 75) {
                strengthBar.style.background = 'var(--warning-color)';
                strengthText.textContent = 'Fair - Add: ' + feedback.join(', ');
                strengthText.style.color = 'var(--warning-color)';
            } else if (score < 100) {
                strengthBar.style.background = 'var(--secondary-color)';
                strengthText.textContent = 'Good - Add: ' + feedback.join(', ');
                strengthText.style.color = 'var(--secondary-color)';
            } else {
                strengthBar.style.background = 'var(--success-color)';
                strengthText.textContent = 'Excellent password strength!';
                strengthText.style.color = 'var(--success-color)';
            }
        });

        // Confirm password validation
        document.getElementById('confirmPassword').addEventListener('input', function() {
            const password = document.getElementById('password').value;
            const confirmPassword = this.value;

            if (confirmPassword && password !== confirmPassword) {
                this.style.borderColor = 'var(--danger-color)';
                this.style.boxShadow = '0 0 0 3px rgba(225, 112, 85, 0.1)';
            } else {
                this.style.borderColor = 'var(--border-color)';
                this.style.boxShadow = 'none';
            }
        });

        // Form validation before submit
        document.getElementById('registerForm').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            // Check password match
            if (password !== confirmPassword) {
                e.preventDefault();
                showAlert('Passwords do not match!', 'danger');
                return false;
            }

            // Check password strength
            if (!isPasswordStrong(password)) {
                e.preventDefault();
                showAlert('Please use a stronger password with at least 8 characters including uppercase, lowercase, number and special character.', 'danger');
                return false;
            }

            // Check terms acceptance
            if (!document.getElementById('terms').checked) {
                e.preventDefault();
                showAlert('Please accept the Terms & Conditions to continue.', 'danger');
                return false;
            }

            // Check age confirmation
            if (!document.getElementById('ageConfirm').checked) {
                e.preventDefault();
                showAlert('You must be 18 or older to create an account.', 'danger');
                return false;
            }

            // Show loading state
            const submitBtn = this.querySelector('button[type="submit"]');
            setButtonLoading(submitBtn, true);
        });

        // Username availability check (simulated)
        document.getElementById('username').addEventListener('blur', function() {
            const username = this.value;
            if (username.length >= 3) {
                // Simulate API call
                setTimeout(() => {
                    // For demo purposes, show username as available
                    showAlert('Username "' + username + '" is available!', 'success');
                }, 500);
            }
        });
    </script>
</body>
</html>