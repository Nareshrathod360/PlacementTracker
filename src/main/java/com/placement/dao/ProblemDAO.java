package com.placement.dao;

import com.placement.model.Problem;
import com.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProblemDAO.java - Data Access Object for Problem operations.
 * Handles all CRUD operations for the "problems" table.
 */
public class ProblemDAO {

    // ── Helper: map a ResultSet row to a Problem object ────────────────────
    private Problem mapRow(ResultSet rs) throws SQLException {
        Problem p = new Problem();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setProblemName(rs.getString("problem_name"));
        p.setPlatform(rs.getString("platform"));
        p.setDifficulty(rs.getString("difficulty"));
        p.setStatus(rs.getString("status"));
        p.setCompany(rs.getString("company"));
        p.setFavorite(rs.getBoolean("is_favorite"));
        p.setDateAdded(rs.getString("date_added"));
        return p;
    }

    /**
     * Add a new problem for a user.
     */
    public boolean addProblem(Problem problem) {
        String sql = "INSERT INTO problems (user_id, problem_name, platform, difficulty, status, company) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problem.getUserId());
            ps.setString(2, problem.getProblemName());
            ps.setString(3, problem.getPlatform());
            ps.setString(4, problem.getDifficulty());
            ps.setString(5, problem.getStatus());
            ps.setString(6, problem.getCompany());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("DB Error in addProblem: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all problems for a specific user.
     * Supports optional filters: difficulty, platform, company, search keyword.
     *
     * @param userId     the logged-in user's ID
     * @param difficulty filter (null = no filter)
     * @param platform   filter (null = no filter)
     * @param company    filter (null = no filter)
     * @param search     keyword search on problem name (null = no filter)
     */
    public List<Problem> getProblems(int userId, String difficulty,
                                     String platform, String company, String search) {
        List<Problem> problems = new ArrayList<>();

        // Build dynamic SQL based on provided filters
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM problems WHERE user_id = ?");

        if (difficulty != null && !difficulty.isEmpty())
            sql.append(" AND difficulty = ?");
        if (platform != null && !platform.isEmpty())
            sql.append(" AND platform = ?");
        if (company != null && !company.isEmpty())
            sql.append(" AND company LIKE ?");
        if (search != null && !search.isEmpty())
            sql.append(" AND problem_name LIKE ?");

        sql.append(" ORDER BY date_added DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Bind parameters in order
            int idx = 1;
            ps.setInt(idx++, userId);
            if (difficulty != null && !difficulty.isEmpty()) ps.setString(idx++, difficulty);
            if (platform  != null && !platform.isEmpty())   ps.setString(idx++, platform);
            if (company   != null && !company.isEmpty())    ps.setString(idx++, "%" + company + "%");
            if (search    != null && !search.isEmpty())     ps.setString(idx++, "%" + search + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                problems.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("DB Error in getProblems: " + e.getMessage());
        }

        return problems;
    }

    /**
     * Get a single problem by its ID (and verify it belongs to the user).
     */
    public Problem getProblemById(int problemId, int userId) {
        String sql = "SELECT * FROM problems WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, problemId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("DB Error in getProblemById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update all fields of a problem.
     */
    public boolean updateProblem(Problem problem) {
        String sql = "UPDATE problems SET problem_name=?, platform=?, difficulty=?, "
                   + "status=?, company=? WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, problem.getProblemName());
            ps.setString(2, problem.getPlatform());
            ps.setString(3, problem.getDifficulty());
            ps.setString(4, problem.getStatus());
            ps.setString(5, problem.getCompany());
            ps.setInt(6, problem.getId());
            ps.setInt(7, problem.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error in updateProblem: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggle the favorite (star) status of a problem.
     */
    public boolean toggleFavorite(int problemId, int userId) {
        String sql = "UPDATE problems SET is_favorite = NOT is_favorite WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, problemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error in toggleFavorite: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a problem (only if it belongs to the user).
     */
    public boolean deleteProblem(int problemId, int userId) {
        String sql = "DELETE FROM problems WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, problemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error in deleteProblem: " + e.getMessage());
            return false;
        }
    }

    // ── Statistics Queries ─────────────────────────────────────────────────

    /** Total number of problems for a user */
    public int getTotalProblems(int userId) {
        return countQuery("SELECT COUNT(*) FROM problems WHERE user_id=?", userId);
    }

    /** Number of solved problems */
    public int getSolvedCount(int userId) {
        return countQuery("SELECT COUNT(*) FROM problems WHERE user_id=? AND status='Solved'", userId);
    }

    /** Number of problems by difficulty and status */
    public int getCountByDifficultyAndStatus(int userId, String difficulty, String status) {
        String sql = "SELECT COUNT(*) FROM problems WHERE user_id=? AND difficulty=? AND status=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, difficulty);
            ps.setString(3, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("DB Error in getCountByDifficultyAndStatus: " + e.getMessage());
        }
        return 0;
    }

    /** Get 5 most recently added/updated problems */
    public List<Problem> getRecentProblems(int userId) {
        List<Problem> problems = new ArrayList<>();
        String sql = "SELECT * FROM problems WHERE user_id=? ORDER BY date_added DESC LIMIT 5";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) problems.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DB Error in getRecentProblems: " + e.getMessage());
        }
        return problems;
    }

    /** Helper for simple COUNT(*) queries with userId parameter */
    private int countQuery(String sql, int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("DB Error in countQuery: " + e.getMessage());
        }
        return 0;
    }

    /** Record today's solve in the streak table if status is 'Solved' */
    public void recordStreak(int userId) {
        String sql = "INSERT INTO daily_streak (user_id, solve_date, problems_solved) VALUES (?, CURDATE(), 1) "
                   + "ON DUPLICATE KEY UPDATE problems_solved = problems_solved + 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB Error in recordStreak: " + e.getMessage());
        }
    }

    /** Get current streak (consecutive days with solves) */
    public int getCurrentStreak(int userId) {
        String sql = "SELECT solve_date FROM daily_streak WHERE user_id=? ORDER BY solve_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            int streak = 0;
            java.time.LocalDate expected = java.time.LocalDate.now();
            while (rs.next()) {
                java.time.LocalDate date = rs.getDate("solve_date").toLocalDate();
                if (date.equals(expected) || date.equals(expected.minusDays(0))) {
                    streak++;
                    expected = date.minusDays(1);
                } else break;
            }
            return streak;
        } catch (SQLException e) {
            System.err.println("DB Error in getCurrentStreak: " + e.getMessage());
            return 0;
        }
    }
}
