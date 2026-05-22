package com.placement.servlet;

import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthErrorServlet.java
 * GET /auth-error → returns any pending error message from the session as JSON.
 * Used by the login page to display server-side error messages.
 */
@WebServlet("/auth-error")
public class AuthErrorServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("errorMsg") != null) {
            result.put("error", session.getAttribute("errorMsg"));
            session.removeAttribute("errorMsg"); // consume the message
        } else {
            result.put("error", null);
        }

        resp.getWriter().write(gson.toJson(result));
    }
}
