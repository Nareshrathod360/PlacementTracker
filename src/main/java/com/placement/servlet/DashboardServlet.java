package com.placement.servlet;

import com.placement.dao.NoteDAO;
import com.placement.dao.ProblemDAO;
import com.placement.model.Problem;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * DashboardServlet.java
 * Provides JSON data for the dashboard page.
 * GET /dashboard-data returns stats + recent activity as JSON
 */
@WebServlet("/dashboard-data")
public class DashboardServlet extends HttpServlet {

    private final ProblemDAO problemDAO = new ProblemDAO();
    private final NoteDAO    noteDAO    = new NoteDAO();
    private final Gson       gson       = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Not logged in\"}");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        Map<String, Object> data = new HashMap<>();

        data.put("totalProblems", problemDAO.getTotalProblems(userId));
        data.put("solvedCount",   problemDAO.getSolvedCount(userId));
        data.put("totalNotes",    noteDAO.getTotalNotes(userId));
        data.put("currentStreak", problemDAO.getCurrentStreak(userId));
        data.put("userName",      session.getAttribute("userName"));

        int easySolved   = problemDAO.getCountByDifficultyAndStatus(userId, "Easy",   "Solved");
        int mediumSolved = problemDAO.getCountByDifficultyAndStatus(userId, "Medium", "Solved");
        int hardSolved   = problemDAO.getCountByDifficultyAndStatus(userId, "Hard",   "Solved");

        Map<String, Object> stats = new HashMap<>();
        stats.put("easySolved",   easySolved);
        stats.put("mediumSolved", mediumSolved);
        stats.put("hardSolved",   hardSolved);
        data.put("stats", stats);

        data.put("easyCount",   countByDiff(userId, "Easy"));
        data.put("mediumCount", countByDiff(userId, "Medium"));
        data.put("hardCount",   countByDiff(userId, "Hard"));

        int attemptedCount = problemDAO.getCountByDifficultyAndStatus(userId, "Easy",   "Attempted")
                           + problemDAO.getCountByDifficultyAndStatus(userId, "Medium", "Attempted")
                           + problemDAO.getCountByDifficultyAndStatus(userId, "Hard",   "Attempted");
        data.put("attemptedCount", attemptedCount);
        data.put("todoCount", problemDAO.getTotalProblems(userId)
                            - problemDAO.getSolvedCount(userId) - attemptedCount);

        List<Problem> recent = problemDAO.getRecentProblems(userId);
        data.put("recentProblems", recent);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(data));
    }

    private int countByDiff(int userId, String diff) {
        return problemDAO.getCountByDifficultyAndStatus(userId, diff, "Solved")
             + problemDAO.getCountByDifficultyAndStatus(userId, diff, "Attempted")
             + problemDAO.getCountByDifficultyAndStatus(userId, diff, "Todo");
    }
}
