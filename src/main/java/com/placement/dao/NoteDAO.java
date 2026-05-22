package com.placement.dao;

import com.placement.model.Note;
import com.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NoteDAO.java - Data Access Object for Note operations.
 * Handles all CRUD operations for the "notes" table.
 */
public class NoteDAO {

    /** Map a ResultSet row to a Note object */
    private Note mapRow(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setUserId(rs.getInt("user_id"));
        note.setTitle(rs.getString("title"));
        note.setContent(rs.getString("content"));
        note.setDateCreated(rs.getString("date_created"));
        note.setDateUpdated(rs.getString("date_updated"));
        return note;
    }

    /** Add a new note */
    public boolean addNote(Note note) {
        String sql = "INSERT INTO notes (user_id, title, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, note.getUserId());
            ps.setString(2, note.getTitle());
            ps.setString(3, note.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error in addNote: " + e.getMessage());
            return false;
        }
    }

    /** Get all notes for a user (newest first), with optional search */
    public List<Note> getNotes(int userId, String search) {
        List<Note> notes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM notes WHERE user_id=?");
        if (search != null && !search.isEmpty())
            sql.append(" AND (title LIKE ? OR content LIKE ?)");
        sql.append(" ORDER BY date_updated DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            if (search != null && !search.isEmpty()) {
                ps.setString(2, "%" + search + "%");
                ps.setString(3, "%" + search + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) notes.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DB Error in getNotes: " + e.getMessage());
        }
        return notes;
    }

    /** Get a single note by ID (verify ownership) */
    public Note getNoteById(int noteId, int userId) {
        String sql = "SELECT * FROM notes WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("DB Error in getNoteById: " + e.getMessage());
        }
        return null;
    }

    /** Update a note's title and content */
    public boolean updateNote(Note note) {
        String sql = "UPDATE notes SET title=?, content=? WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            ps.setInt(3, note.getId());
            ps.setInt(4, note.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error in updateNote: " + e.getMessage());
            return false;
        }
    }

    /** Delete a note */
    public boolean deleteNote(int noteId, int userId) {
        String sql = "DELETE FROM notes WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error in deleteNote: " + e.getMessage());
            return false;
        }
    }

    /** Count total notes for a user */
    public int getTotalNotes(int userId) {
        String sql = "SELECT COUNT(*) FROM notes WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("DB Error in getTotalNotes: " + e.getMessage());
        }
        return 0;
    }
}
