package com.example.mycourseschedule.Models;

import java.util.Date;

public class Term {
    //variables
    private int id;
    private String title;
    private Date startDate;
    private Date endDate;

    //object constructor
    public Term(String title, Date startDate, Date endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    //setters and getters for Term.
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}