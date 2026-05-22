package com.placement.servlet;

import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SessionServlet.java
 * GET /session → returns current session info as JSON
 * Used by frontend to check if user is logged in and get their name.
 */
@WebServlet("/session")
public class SessionServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            result.put("loggedIn",  true);
            result.put("userId",    session.getAttribute("userId"));
            result.put("userName",  session.getAttribute("userName"));
            result.put("userEmail", session.getAttribute("userEmail"));
        } else {
            result.put("loggedIn", false);
        }

        resp.getWriter().write(gson.toJson(result));
    }
}
