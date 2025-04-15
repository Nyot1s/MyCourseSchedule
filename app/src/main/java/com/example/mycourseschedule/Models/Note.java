package com.example.mycourseschedule.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int courseId;
    private String content;

    public Note(int courseId, String content) {
        this.courseId = courseId;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}