package com.example.mycourseschedule.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mycourseschedule.Models.Assessment;

import java.util.List;

@Dao
public interface AssessmentDao {
    @Insert
    void insert(Assessment assessment);

    @Update
    void update(Assessment assessment);

    @Delete
    void delete(Assessment assessment);

    @Query("SELECT * FROM assessments WHERE courseId = :courseId")
    LiveData<List<Assessment>> getAssessmentsForCourse(int courseId);

    @Query("SELECT COUNT(*) FROM assessments WHERE courseId = :courseId")
    int getAssessmentCount(int courseId);
}