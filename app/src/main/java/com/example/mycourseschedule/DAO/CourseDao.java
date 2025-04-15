package com.example.mycourseschedule.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mycourseschedule.Models.Course;

import java.util.List;

@Dao
public interface CourseDao {
    @Insert
    void insert(Course course);
    @Delete
    void delete(Course course);
    @Update
    void update(Course course);

    @Query("SELECT * FROM courses WHERE termId = :termId")
    LiveData<List<Course>> getCoursesForTerm(int termId);

    // Add this synchronous method to fetch a course by ID
    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    Course getCourseById(int courseId);



}
