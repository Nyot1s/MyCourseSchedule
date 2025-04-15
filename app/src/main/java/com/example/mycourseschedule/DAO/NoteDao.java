package com.example.mycourseschedule.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mycourseschedule.Models.Note;

@Dao
public interface NoteDao {
    @Insert void insert(Note note);
    @Update void update(Note note);
    @Delete void delete(Note note);
    @Query("SELECT * FROM notes WHERE courseId = :courseId LIMIT 1")
    Note getNoteByCourseId(int courseId);
}