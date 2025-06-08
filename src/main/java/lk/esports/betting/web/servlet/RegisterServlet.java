package lk.esports.betting.web.servlet;

import lk.esports.betting.ejb.local.UserService;
import lk.esports.betting.entity.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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

    private UserService userService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("RegisterServlet initializing...");

        try {
            InitialContext ctx = new InitialContext();

            // Try multiple lookup paths for UserService
            String[] lookupPaths = {
                    "java:comp/env/ejb/UserService",
                    "java:global/ESportsBetting/UserServiceBean",
                    "java:app/ESportsBetting/UserServiceBean",
                    "java:module/UserServiceBean"
            };

            for (String path : lookupPaths) {
                try {
                    userService = (UserService) ctx.lookup(path);
                    if (userService != null) {
                        logger.info("UserService found at: " + path);
                        break;
                    }
                } catch (NamingException e) {
                    logger.warning("Failed UserService lookup at: " + path + " - " + e.getMessage());
                }
            }

            if (userService == null) {
                logger.severe("UserService EJB lookup failed at all paths!");
            } else {
                logger.info("UserService EJB lookup successful in RegisterServlet");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during UserService EJB lookup in RegisterServlet", e);
        }
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

            // Check if UserService is available
            if (userService == null) {
                logger.severe("UserService is null - EJB lookup failed in RegisterServlet");
                request.setAttribute("errorMessage", "System is initializing. Please try again in a moment.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Check if email or username already exists
            try {
                if (userService.isEmailExists(email.trim())) {
                    request.setAttribute("errorMessage", "Email address is already registered");
                    preserveFormData(request, email, username, fullName, phone);
                    request.getRequestDispatcher("/register.jsp").forward(request, response);
                    return;
                }

                if (userService.isUsernameExists(username.trim())) {
                    request.setAttribute("errorMessage", "Username is already taken");
                    preserveFormData(request, email, username, fullName, phone);
                    request.getRequestDispatcher("/register.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking existing user data", e);
                request.setAttribute("errorMessage", "Database error. Please try again later.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Register new user
            User newUser = userService.registerUser(
                    email.trim(),
                    username.trim(),
                    password,
                    fullName.trim(),
                    phone != null ? phone.trim() : null
            );

            if (newUser != null) {
                // Registration successful - add welcome bonus
                try {
                    userService.addFunds(newUser.getId(), new BigDecimal("100.00"), "Welcome bonus");

                    // Update user balance
                    newUser = userService.findUserById(newUser.getId());
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
                        "Welcome to E-Sports Betting! Your account has been created and you've received a $100 welcome bonus.");

                response.sendRedirect(request.getContextPath() + "/dashboard");

            } else {
                request.setAttribute("errorMessage", "Registration failed. Please try again.");
                preserveFormData(request, email, username, fullName, phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
            }

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Registration validation error: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            preserveFormData(request, email, username, fullName, phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);

        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Runtime error during registration", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("email already exists")) {
                request.setAttribute("errorMessage", "This email address is already registered. Please use a different email or try logging in.");
            } else if (errorMsg != null && errorMsg.contains("username already exists")) {
                request.setAttribute("errorMessage", "This username is already taken. Please choose a different username.");
            } else {
                request.setAttribute("errorMessage", "Registration failed due to a system error. Please try again.");
            }
            preserveFormData(request, email, username, fullName, phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during registration", e);
            request.setAttribute("errorMessage", "An unexpected error occurred during registration. Please try again later.");
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