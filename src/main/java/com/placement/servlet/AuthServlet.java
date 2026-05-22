package com.placement.servlet;

import com.placement.dao.UserDAO;
import com.placement.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AuthServlet.java - Handles user authentication:
 *   POST /auth?action=register  → Register new user
 *   POST /auth?action=login     → Login existing user
 *   GET  /auth?action=logout    → Logout current user
 */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("logout".equals(action)) {
            // Invalidate session on logout
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/index.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("register".equals(action)) {
            handleRegister(req, resp);
        } else if ("login".equals(action)) {
            handleLogin(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/index.html");
        }
    }

    /** Handle user registration */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String name     = req.getParameter("name").trim();
        String email    = req.getParameter("email").trim().toLowerCase();
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirmPassword");

        // Basic server-side validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            redirectWithError(resp, req, "index.html", "All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            redirectWithError(resp, req, "index.html", "Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            redirectWithError(resp, req, "index.html", "Password must be at least 6 characters.");
            return;
        }

        User user = new User(name, email, password);
        boolean success = userDAO.registerUser(user);

        if (success) {
            // Auto-login after registration
            User loggedIn = userDAO.loginUser(email, password);
            createSession(req, loggedIn);
            resp.sendRedirect(req.getContextPath() + "/dashboard.html");
        } else {
            redirectWithError(resp, req, "index.html", "Email already registered. Please login.");
        }
    }

    /** Handle user login */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String email    = req.getParameter("email").trim().toLowerCase();
        String password = req.getParameter("password");

        if (email.isEmpty() || password.isEmpty()) {
            redirectWithError(resp, req, "index.html", "Email and password are required.");
            return;
        }

        User user = userDAO.loginUser(email, password);

        if (user != null) {
            createSession(req, user);
            resp.sendRedirect(req.getContextPath() + "/dashboard.html");
        } else {
            redirectWithError(resp, req, "index.html", "Invalid email or password.");
        }
    }

    /** Create a new session and store user info */
    private void createSession(HttpServletRequest req, User user) {
        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
    }

    /** Redirect back with an error message stored in session */
    private void redirectWithError(HttpServletResponse resp, HttpServletRequest req,
                                   String page, String message) throws IOException {
        HttpSession session = req.getSession(true);
        session.setAttribute("errorMsg", message);
        resp.sendRedirect(req.getContextPath() + "/" + page);
    }
}
