package com.example.mycourseschedule.Helper;

import androidx.room.Database;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.mycourseschedule.DAO.AssessmentDao;
import com.example.mycourseschedule.DAO.CourseDao;
import com.example.mycourseschedule.DAO.TermDao;
import com.example.mycourseschedule.Models.Assessment;
import com.example.mycourseschedule.Models.Course;
import com.example.mycourseschedule.Models.Term;


@Database(entities = {Term.class, Course.class, Assessment.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TermDao termDao();
    public abstract CourseDao courseDao();
    public abstract AssessmentDao assessmentDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "course-planner-db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}