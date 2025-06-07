package lk.esports.betting.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Authentication filter that checks if user is logged in before accessing protected resources
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/dashboard",
        "/dashboard/*",
        "/bets/*",
        "/matches/details/*",
        "/profile",
        "/profile/*",
        "/wallet",
        "/wallet/*"
})
public class AuthFilter implements Filter {

    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());

    // URLs that don't require authentication even if they match the pattern
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/matches/upcoming",
            "/matches/live",
            "/matches/completed",
            "/matches/teams",
            "/matches/tournaments"
    );

    // AJAX endpoints that should return JSON error instead of redirect
    private static final List<String> AJAX_PATHS = Arrays.asList(
            "/bets/place",
            "/bets/cancel",
            "/bets/calculate"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Check if this path should be excluded from authentication
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get session
        HttpSession session = request.getSession(false);

        // Check if user is authenticated
        boolean isAuthenticated = session != null &&
                session.getAttribute("userId") != null &&
                session.getAttribute("user") != null;

        if (isAuthenticated) {
            // User is authenticated, check if session is still valid
            try {
                Long userId = (Long) session.getAttribute("userId");
                String userEmail = (String) session.getAttribute("userEmail");

                if (userId != null && userEmail != null) {
                    // Add user info to request for easy access in servlets
                    request.setAttribute("currentUserId", userId);
                    request.setAttribute("currentUserEmail", userEmail);

                    // Continue with the request
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                logger.warning("Error validating session: " + e.getMessage());
                // Clear invalid session
                session.invalidate();
                isAuthenticated = false;
            }
        }

        // User is not authenticated
        if (!isAuthenticated) {
            handleUnauthenticatedRequest(request, response, path);
        }
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xmlHttpRequest = request.getHeader("X-Requested-With");
        String contentType = request.getContentType();
        String accept = request.getHeader("Accept");

        return "XMLHttpRequest".equals(xmlHttpRequest) ||
                (contentType != null && contentType.contains("application/json")) ||
                (accept != null && accept.contains("application/json"));
    }

    private boolean isAjaxPath(String path) {
        return AJAX_PATHS.stream().anyMatch(path::startsWith);
    }

    private void handleUnauthenticatedRequest(HttpServletRequest request, HttpServletResponse response,
                                              String path) throws IOException {

        // Store the requested URL for redirect after login
        HttpSession session = request.getSession(true);
        String requestedUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        if (queryString != null) {
            requestedUrl += "?" + queryString;
        }

        session.setAttribute("redirectAfterLogin", requestedUrl);

        // Check if this is an AJAX request or AJAX endpoint
        if (isAjaxRequest(request) || isAjaxPath(path)) {
            // Return JSON error for AJAX requests
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonError = "{\"success\": false, \"error\": \"Authentication required\", \"redirectUrl\": \"" +
                    request.getContextPath() + "/login\"}";

            response.getWriter().write(jsonError);
            response.getWriter().flush();
        } else {
            // Redirect to login page for regular requests
            response.sendRedirect(request.getContextPath() + "/login");
        }

        logger.info("Unauthorized access attempt to: " + path + " from IP: " + request.getRemoteAddr());
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}