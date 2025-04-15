package com.example.mycourseschedule.Models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "courses", foreignKeys = @ForeignKey(entity = Term.class, parentColumns = "id", childColumns = "termId"))
public class Course {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int termId;
    private String title;
    private String status; //"In Progress, "Completed", "Dropped", "Plan to take"
    private Date startDate;
    private Date endDate;
    private String instructorName;
    private String instructorPhone;
    private String instructorEmail;
    private String note;


    //Setters and Getters
    //Tool tip, use alt + insert to generate Setters and Getters, saved me 10 minutes, saving this comment here to remember how I did this.
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getInstructorPhone() {
        return instructorPhone;
    }

    public void setInstructorPhone(String instructorPhone) {
        this.instructorPhone = instructorPhone;
    }

    public String getInstructorEmail() {
        return instructorEmail;
    }

    public void setInstructorEmail(String instructorEmail) {
        this.instructorEmail = instructorEmail;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
