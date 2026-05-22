package com.placement.servlet;

import com.placement.dao.ProblemDAO;
import com.placement.model.Problem;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProblemServlet.java - REST-style controller for Problem CRUD.
 *
 * GET  /problems              → list problems (with optional filters)
 * POST /problems?action=add   → add new problem
 * POST /problems?action=edit  → update problem
 * POST /problems?action=delete→ delete problem
 * POST /problems?action=star  → toggle favorite
 */
@WebServlet("/problems")
public class ProblemServlet extends HttpServlet {

    private final ProblemDAO problemDAO = new ProblemDAO();
    private final Gson       gson       = new Gson();

    // ── GET: fetch list of problems ────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int userId = getUserId(req, resp);
        if (userId == -1) return;

        // Read optional filter params
        String difficulty = req.getParameter("difficulty");
        String platform   = req.getParameter("platform");
        String company    = req.getParameter("company");
        String search     = req.getParameter("search");

        List<Problem> problems = problemDAO.getProblems(userId, difficulty, platform, company, search);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(problems));
    }

    // ── POST: add / edit / delete / star ──────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int userId = getUserId(req, resp);
        if (userId == -1) return;

        String action = req.getParameter("action");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();

        switch (action == null ? "" : action) {
            case "add":
                result = handleAdd(req, userId);
                break;
            case "edit":
                result = handleEdit(req, userId);
                break;
            case "delete":
                result = handleDelete(req, userId);
                break;
            case "star":
                result = handleStar(req, userId);
                break;
            default:
                result.put("success", false);
                result.put("message", "Unknown action");
        }

        resp.getWriter().write(gson.toJson(result));
    }

    /** Add new problem */
    private Map<String, Object> handleAdd(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Problem p = buildProblemFromRequest(req, userId);
            boolean ok = problemDAO.addProblem(p);
            // If the status is Solved, record streak
            if (ok && "Solved".equals(p.getStatus())) {
                problemDAO.recordStreak(userId);
            }
            result.put("success", ok);
            result.put("message", ok ? "Problem added successfully!" : "Failed to add problem.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        return result;
    }

    /** Edit existing problem */
    private Map<String, Object> handleEdit(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int problemId = Integer.parseInt(req.getParameter("id"));
            Problem p = buildProblemFromRequest(req, userId);
            p.setId(problemId);
            boolean ok = problemDAO.updateProblem(p);
            if (ok && "Solved".equals(p.getStatus())) {
                problemDAO.recordStreak(userId);
            }
            result.put("success", ok);
            result.put("message", ok ? "Problem updated!" : "Update failed.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        return result;
    }

    /** Delete a problem */
    private Map<String, Object> handleDelete(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int problemId = Integer.parseInt(req.getParameter("id"));
            boolean ok = problemDAO.deleteProblem(problemId, userId);
            result.put("success", ok);
            result.put("message", ok ? "Problem deleted." : "Delete failed.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        return result;
    }

    /** Toggle favorite star */
    private Map<String, Object> handleStar(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int problemId = Integer.parseInt(req.getParameter("id"));
            boolean ok = problemDAO.toggleFavorite(problemId, userId);
            result.put("success", ok);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    /** Build a Problem object from HTTP request parameters */
    private Problem buildProblemFromRequest(HttpServletRequest req, int userId) {
        Problem p = new Problem();
        p.setUserId(userId);
        p.setProblemName(req.getParameter("problemName").trim());
        p.setPlatform(req.getParameter("platform").trim());
        p.setDifficulty(req.getParameter("difficulty"));
        p.setStatus(req.getParameter("status"));
        String company = req.getParameter("company");
        p.setCompany(company != null ? company.trim() : "");
        return p;
    }

    /** Get userId from session, send 401 if not logged in */
    private int getUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Not logged in\"}");
            return -1;
        }
        return (int) session.getAttribute("userId");
    }
}
