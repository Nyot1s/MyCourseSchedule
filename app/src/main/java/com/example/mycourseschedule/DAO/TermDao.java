package com.example.mycourseschedule.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mycourseschedule.Models.Term;

import java.util.List;

@Dao
public interface TermDao {
    @Insert void insert(Term term);
    @Update void update(Term term);
    @Delete void delete(Term term);
    @Query("SELECT * FROM terms") LiveData<List<Term>> getAllTerms();
    @Query("SELECT COUNT(*) FROM courses WHERE termId = :termId") int getCourseCount(int termId);
}
