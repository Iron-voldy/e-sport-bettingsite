package lk.esports.betting.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet to handle CSS, JS and other static resources
 * This ensures proper content-type headers and caching
 */
@WebServlet({"/css/*", "/js/*", "/images/*", "/fonts/*"})
public class StaticResourceServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(StaticResourceServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String resourcePath = requestURI.substring(contextPath.length());

        logger.info("Serving static resource: " + resourcePath);

        // Set proper content type based on file extension
        String contentType = getContentType(resourcePath);
        response.setContentType(contentType);

        // Set caching headers for better performance
        response.setHeader("Cache-Control", "public, max-age=31536000"); // 1 year
        response.setHeader("Expires", "Thu, 31 Dec 2025 23:59:59 GMT");

        // Try to serve from webapp directory first
        try (InputStream inputStream = getServletContext().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                copyStream(inputStream, response.getOutputStream());
                return;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error serving resource from webapp: " + resourcePath, e);
        }

        // If not found, return 404
        logger.warning("Resource not found: " + resourcePath);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private String getContentType(String resourcePath) {
        String lower = resourcePath.toLowerCase();

        if (lower.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (lower.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lower.endsWith(".woff")) {
            return "application/font-woff";
        } else if (lower.endsWith(".woff2")) {
            return "application/font-woff2";
        } else if (lower.endsWith(".ttf")) {
            return "application/font-ttf";
        } else if (lower.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        } else {
            return "application/octet-stream";
        }
    }

    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }
}