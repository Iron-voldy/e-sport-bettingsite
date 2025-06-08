package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.entity.User;
import lk.esports.betting.utils.EJBServiceLocator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$");

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("RegisterServlet initializing...");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // Forward to registration page
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String terms = request.getParameter("terms");

        try {
            // Validate input
            String validationError = validateRegistrationInput(
                    email, username, password, confirmPassword, fullName, phone, terms);

            if (validationError != null) {
                request.setAttribute("errorMessage", validationError);
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Get UserService using service locator
            UserService userService = EJBServiceLocator.getUserService();

            if (userService == null) {
                logger.severe("UserService is null - service locator failed");
                request.setAttribute("errorMessage", "Registration service is temporarily unavailable. Please try again.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Check if email or username already exists
            try {
                if (userService.isEmailExists(email.trim())) {
                    request.setAttribute("errorMessage", "Email address is already registered. Please use a different email or try logging in.");
                    preserveFormData(request, email, username, fullName, phone);
                    request.getRequestDispatcher("/register.jsp").forward(request, response);
                    return;
                }

                if (userService.isUsernameExists(username.trim())) {
                    request.setAttribute("errorMessage", "Username is already taken. Please choose a different username.");
                    preserveFormData(request, email, username, fullName, phone);
                    request.getRequestDispatcher("/register.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking existing user data", e);
                request.setAttribute("errorMessage", "Unable to verify user data. Please try again.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Register new user
            User newUser = null;
            try {
                newUser = userService.registerUser(
                        email.trim(),
                        username.trim(),
                        password,
                        fullName.trim(),
                        phone != null ? phone.trim() : null
                );
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Registration validation error: " + e.getMessage());
                request.setAttribute("errorMessage", e.getMessage());
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Runtime error during registration", e);
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("email already exists")) {
                    request.setAttribute("errorMessage", "This email address is already registered. Please use a different email or try logging in.");
                } else if (errorMsg != null && errorMsg.contains("username already exists")) {
                    request.setAttribute("errorMessage", "This username is already taken. Please choose a different username.");
                } else {
                    request.setAttribute("errorMessage", "Registration failed. Please try again.");
                }
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error during registration", e);
                request.setAttribute("errorMessage", "Registration failed due to a system error. Please try again later.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            if (newUser != null) {
                // Registration successful - add welcome bonus
                try {
                    userService.addFunds(newUser.getId(), new BigDecimal("100.00"), "Welcome bonus");

                    // Refresh user to get updated balance
                    newUser = userService.findUserById(newUser.getId());

                    logger.info("Welcome bonus added for new user: " + newUser.getUsername());
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to add welcome bonus for user: " + newUser.getId(), e);
                    // Continue anyway - registration was successful
                }

                // Auto-login the user
                HttpSession session = request.getSession(true);
                session.setAttribute("user", newUser);
                session.setAttribute("userId", newUser.getId());
                session.setAttribute("username", newUser.getUsername());
                session.setAttribute("userEmail", newUser.getEmail());
                session.setAttribute("userBalance", newUser.getWalletBalance());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                logger.info("New user registered and logged in: " + newUser.getEmail());

                // Set success message for dashboard
                session.setAttribute("successMessage",
                        "Welcome to E-Sports Betting! Your account has been created successfully" +
                                (newUser.getWalletBalance().compareTo(BigDecimal.ZERO) > 0 ?
                                        " and you've received a $100 welcome bonus." : "."));

                response.sendRedirect(request.getContextPath() + "/dashboard");

            } else {
                request.setAttribute("errorMessage", "Registration failed. Please try again.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in registration servlet", e);
            request.setAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            preserveFormData(request, email, username, fullName, phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private String validateRegistrationInput(String email, String username, String password,
                                             String confirmPassword, String fullName, String phone, String terms) {

        // Check required fields
        if (email == null || email.trim().isEmpty()) {
            return "Email address is required";
        }
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Password confirmation is required";
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Full name is required";
        }
        if (terms == null || !"on".equals(terms)) {
            return "You must accept the terms and conditions";
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Please enter a valid email address";
        }

        // Validate username format
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "Username must be 3-20 characters long and contain only letters, numbers, and underscores";
        }

        // Validate password
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (password.length() > 100) {
            return "Password is too long (maximum 100 characters)";
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        // Validate full name
        if (fullName.trim().length() < 2) {
            return "Full name must be at least 2 characters long";
        }
        if (fullName.trim().length() > 100) {
            return "Full name is too long (maximum 100 characters)";
        }

        // Validate phone number (optional)
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                return "Please enter a valid phone number";
            }
        }

        return null; // No validation errors
    }

    private void preserveFormData(HttpServletRequest request, String email, String username,
                                  String fullName, String phone) {
        request.setAttribute("email", email);
        request.setAttribute("username", username);
        request.setAttribute("fullName", fullName);
        request.setAttribute("phone", phone);
    }
}