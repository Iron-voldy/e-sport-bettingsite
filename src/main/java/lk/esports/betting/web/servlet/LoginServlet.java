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
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("LoginServlet initializing...");
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

        // Forward to login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        try {
            // Validate input
            if (email == null || email.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {

                request.setAttribute("errorMessage", "Email and password are required");
                request.setAttribute("email", email); // Preserve email
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            // Get UserService using service locator
            UserService userService = EJBServiceLocator.getUserService();

            if (userService == null) {
                logger.severe("UserService is null - service locator failed");
                request.setAttribute("errorMessage", "System is initializing. Please try again in a moment.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            // Authenticate user
            User user = userService.authenticateUser(email.trim(), password);

            if (user != null) {
                // Authentication successful
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userBalance", user.getWalletBalance());

                // Set session timeout (30 minutes)
                session.setMaxInactiveInterval(30 * 60);

                // Handle remember me functionality
                if ("on".equals(rememberMe)) {
                    session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 days
                }

                logger.info("User logged in successfully: " + user.getEmail());

                // Check for redirect URL in session
                String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
                if (redirectUrl != null) {
                    session.removeAttribute("redirectAfterLogin");
                    response.sendRedirect(redirectUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/dashboard");
                }

            } else {
                // Authentication failed
                request.setAttribute("errorMessage", "Invalid email or password");
                request.setAttribute("email", email); // Preserve email for user convenience

                logger.warning("Failed login attempt for email: " + email);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during login process", e);
            request.setAttribute("errorMessage", "An error occurred during login. Please try again.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}