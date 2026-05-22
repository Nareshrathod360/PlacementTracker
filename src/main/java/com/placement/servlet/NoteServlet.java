package com.placement.servlet;

import com.placement.dao.NoteDAO;
import com.placement.model.Note;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NoteServlet.java - REST-style controller for Notes CRUD.
 *
 * GET  /notes              → list notes (with optional search)
 * POST /notes?action=add   → add note
 * POST /notes?action=edit  → update note
 * POST /notes?action=delete→ delete note
 */
@WebServlet("/notes")
public class NoteServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();
    private final Gson    gson    = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int userId = getUserId(req, resp);
        if (userId == -1) return;

        String search = req.getParameter("search");
        List<Note> notes = noteDAO.getNotes(userId, search);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(notes));
    }

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
            default:
                result.put("success", false);
                result.put("message", "Unknown action");
        }

        resp.getWriter().write(gson.toJson(result));
    }

    private Map<String, Object> handleAdd(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        String title   = req.getParameter("title");
        String content = req.getParameter("content");

        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Title is required.");
            return result;
        }

        Note note = new Note(userId, title.trim(), content != null ? content : "");
        boolean ok = noteDAO.addNote(note);
        result.put("success", ok);
        result.put("message", ok ? "Note saved!" : "Failed to save note.");
        return result;
    }

    private Map<String, Object> handleEdit(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int noteId = Integer.parseInt(req.getParameter("id"));
            String title   = req.getParameter("title");
            String content = req.getParameter("content");
            Note note = new Note(userId, title.trim(), content);
            note.setId(noteId);
            boolean ok = noteDAO.updateNote(note);
            result.put("success", ok);
            result.put("message", ok ? "Note updated!" : "Update failed.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> handleDelete(HttpServletRequest req, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int noteId = Integer.parseInt(req.getParameter("id"));
            boolean ok = noteDAO.deleteNote(noteId, userId);
            result.put("success", ok);
            result.put("message", ok ? "Note deleted." : "Delete failed.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        return result;
    }

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
