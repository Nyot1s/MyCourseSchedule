package com.example.mycourseschedule.Helper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mycourseschedule.DAO.AssessmentDao;
import com.example.mycourseschedule.DAO.CourseDao;
import com.example.mycourseschedule.DAO.NoteDao;
import com.example.mycourseschedule.DAO.TermDao;
import com.example.mycourseschedule.Models.Assessment;
import com.example.mycourseschedule.Helper.Converters;
import com.example.mycourseschedule.Models.Course;
import com.example.mycourseschedule.Models.Note;
import com.example.mycourseschedule.Models.Term;

@Database(entities = {Term.class, Course.class, Assessment.class, Note.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TermDao termDao();
    public abstract CourseDao courseDao();
    public abstract AssessmentDao assessmentDao();
    public abstract NoteDao noteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .build();
        }
        return instance;
    }
}