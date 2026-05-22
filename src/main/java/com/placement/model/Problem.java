package com.placement.model;

/**
 * Problem.java - Model class representing a coding problem.
 * Maps to the "problems" table in the database.
 */
public class Problem {
    private int id;
    private int userId;
    private String problemName;
    private String platform;       // LeetCode, HackerRank, CodeChef, etc.
    private String difficulty;     // Easy, Medium, Hard
    private String status;         // Solved, Attempted, Todo
    private String company;        // Google, Amazon, Microsoft, etc.
    private boolean isFavorite;    // true = starred problem
    private String dateAdded;

    // ── Constructors ───────────────────────────────────────────────────────

    public Problem() {}

    public Problem(int userId, String problemName, String platform,
                   String difficulty, String status, String company) {
        this.userId = userId;
        this.problemName = problemName;
        this.platform = platform;
        this.difficulty = difficulty;
        this.status = status;
        this.company = company;
    }

    // ── Getters and Setters ────────────────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }

    public String getProblemName()                       { return problemName; }
    public void setProblemName(String problemName)       { this.problemName = problemName; }

    public String getPlatform()                          { return platform; }
    public void setPlatform(String platform)             { this.platform = platform; }

    public String getDifficulty()                        { return difficulty; }
    public void setDifficulty(String difficulty)         { this.difficulty = difficulty; }

    public String getStatus()                            { return status; }
    public void setStatus(String status)                 { this.status = status; }

    public String getCompany()                           { return company; }
    public void setCompany(String company)               { this.company = company; }

    public boolean isFavorite()                          { return isFavorite; }
    public void setFavorite(boolean favorite)            { isFavorite = favorite; }

    public String getDateAdded()                         { return dateAdded; }
    public void setDateAdded(String dateAdded)           { this.dateAdded = dateAdded; }
}
