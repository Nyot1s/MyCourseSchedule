package com.example.mycourseschedule.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "assessments")
public class Assessment {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int courseId;
    private String title;
    private String type; // "performance" or "objective"
    private Date startDate;
    private Date endDate;

    public Assessment(int courseId, String title, String type, Date startDate, Date endDate) {
        this.courseId = courseId;
        this.title = title;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}