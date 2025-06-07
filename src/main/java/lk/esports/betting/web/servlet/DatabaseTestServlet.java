package lk.esports.betting.web.servlet;

import lk.esports.betting.utils.DatabaseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/test/database")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Database Test</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println("h2 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }");
        out.println("h3 { color: #666; margin-top: 30px; }");
        out.println(".success { color: #4CAF50; font-weight: bold; }");
        out.println(".error { color: #f44336; font-weight: bold; }");
        out.println(".warning { color: #ff9800; font-weight: bold; }");
        out.println("table { border-collapse: collapse; width: 100%; margin-top: 10px; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println("pre { background: #f4f4f4; padding: 10px; border-radius: 5px; overflow-x: auto; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<div class='container'>");
        out.println("<h2>🔧 Database Connection Test</h2>");

        try {
            // Test basic connection
            boolean isHealthy = DatabaseUtil.isDatabaseHealthy();
            out.println("<p><strong>Database Health Check:</strong> " +
                    (isHealthy ? "<span class='success'>✅ PASSED</span>" : "<span class='error'>❌ FAILED</span>") + "</p>");

            // Test direct connection
            try (Connection conn = DatabaseUtil.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                out.println("<h3>📊 Database Information</h3>");
                out.println("<p><strong>Database URL:</strong> " + metaData.getURL() + "</p>");
                out.println("<p><strong>Database Product:</strong> " + metaData.getDatabaseProductName() + "</p>");
                out.println("<p><strong>Database Version:</strong> " + metaData.getDatabaseProductVersion() + "</p>");
                out.println("<p><strong>Driver Name:</strong> " + metaData.getDriverName() + "</p>");
                out.println("<p><strong>Connection Valid:</strong> " +
                        (conn.isValid(5) ? "<span class='success'>✅ Yes</span>" : "<span class='error'>❌ No</span>") + "</p>");

                // Test if database exists
                out.println("<h3>🗄️ Database Tables</h3>");
                out.println("<ul>");

                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                boolean hasUsersTable = false;
                int tableCount = 0;
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    out.println("<li>" + tableName + "</li>");
                    tableCount++;
                    if (tableName.equals("users")) {
                        hasUsersTable = true;
                    }
                }
                out.println("</ul>");
                out.println("<p><strong>Total Tables:</strong> " + tableCount + "</p>");

                if (!hasUsersTable) {
                    out.println("<p class='warning'><strong>⚠️ Warning:</strong> 'users' table not found. Database schema may not be created.</p>");
                    out.println("<h3>🛠️ Solution</h3>");
                    out.println("<p>Run the following SQL script to create the required tables:</p>");
                    out.println("<pre>");
                    out.println("CREATE DATABASE IF NOT EXISTS esports_betting;");
                    out.println("USE esports_betting;");
                    out.println("-- Then run the complete schema from database_schema.sql");
                    out.println("</pre>");
                } else {
                    // Test user table structure
                    try (Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery("DESCRIBE users");
                        out.println("<h3>👤 Users Table Structure</h3>");
                        out.println("<table>");
                        out.println("<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>");
                        while (rs.next()) {
                            out.println("<tr>");
                            out.println("<td>" + rs.getString("Field") + "</td>");
                            out.println("<td>" + rs.getString("Type") + "</td>");
                            out.println("<td>" + rs.getString("Null") + "</td>");
                            out.println("<td>" + rs.getString("Key") + "</td>");
                            out.println("<td>" + (rs.getString("Default") != null ? rs.getString("Default") : "NULL") + "</td>");
                            out.println("<td>" + (rs.getString("Extra") != null ? rs.getString("Extra") : "") + "</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");

                        // Test count
                        ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
                        if (countRs.next()) {
                            int userCount = countRs.getInt("count");
                            out.println("<p><strong>Current users count:</strong> " + userCount + "</p>");

                            if (userCount == 0) {
                                out.println("<p class='warning'><strong>ℹ️ Info:</strong> No users in database yet. This is normal for a fresh installation.</p>");
                            }
                        }

                        // Test other required tables
                        String[] requiredTables = {"teams", "tournaments", "matches", "bets", "transactions"};
                        out.println("<h3>📋 Required Tables Check</h3>");
                        out.println("<ul>");
                        for (String tableName : requiredTables) {
                            try {
                                ResultSet tableCheck = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                                if (tableCheck.next()) {
                                    int count = tableCheck.getInt(1);
                                    out.println("<li><span class='success'>✅ " + tableName + "</span> (" + count + " records)</li>");
                                }
                            } catch (Exception e) {
                                out.println("<li><span class='error'>❌ " + tableName + "</span> - Table missing</li>");
                            }
                        }
                        out.println("</ul>");
                    }
                }

                // Test EntityManager
                out.println("<h3>🏗️ Hibernate/JPA Test</h3>");
                try {
                    boolean emTest = DatabaseUtil.verifyDatabaseAccess();
                    out.println("<p><strong>EntityManager Test:</strong> " +
                            (emTest ? "<span class='success'>✅ PASSED</span>" : "<span class='error'>❌ FAILED</span>") + "</p>");
                } catch (Exception e) {
                    out.println("<p><strong>EntityManager Test:</strong> <span class='error'>❌ FAILED</span></p>");
                    out.println("<p class='error'>Error: " + e.getMessage() + "</p>");
                }

            }

        } catch (Exception e) {
            out.println("<p class='error'><strong>❌ Database Connection Error:</strong></p>");
            out.println("<p class='error'>" + e.getMessage() + "</p>");
            out.println("<h3>🔧 Troubleshooting Steps</h3>");
            out.println("<ol>");
            out.println("<li>Ensure MySQL server is running on localhost:3306</li>");
            out.println("<li>Verify database 'esports_betting' exists</li>");
            out.println("<li>Check username 'root' and password '2009928' are correct</li>");
            out.println("<li>Run the database schema SQL script</li>");
            out.println("</ol>");
            out.println("<h3>📋 Full Error Details</h3>");
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
        }

        out.println("<hr>");
        out.println("<p><strong>🕒 Test completed at:</strong> " + new java.util.Date() + "</p>");
        out.println("<p><a href='" + request.getContextPath() + "/' style='color: #4CAF50; text-decoration: none;'>← Back to Home</a></p>");
        out.println("</div>");
        out.println("</body></html>");
    }
}