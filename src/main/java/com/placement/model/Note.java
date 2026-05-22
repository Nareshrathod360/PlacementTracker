package com.placement.model;

/**
 * Note.java - Model class representing a user's note.
 * Maps to the "notes" table in the database.
 */
public class Note {
    private int id;
    private int userId;
    private String title;
    private String content;
    private String dateCreated;
    private String dateUpdated;

    // ── Constructors ───────────────────────────────────────────────────────

    public Note() {}

    public Note(int userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    // ── Getters and Setters ────────────────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }

    public String getTitle()                    { return title; }
    public void setTitle(String title)          { this.title = title; }

    public String getContent()                  { return content; }
    public void setContent(String content)      { this.content = content; }

    public String getDateCreated()              { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }

    public String getDateUpdated()              { return dateUpdated; }
    public void setDateUpdated(String dateUpdated) { this.dateUpdated = dateUpdated; }
}
