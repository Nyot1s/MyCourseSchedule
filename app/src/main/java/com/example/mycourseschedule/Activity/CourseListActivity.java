package com.example.mycourseschedule.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycourseschedule.Adapter.CourseAdapter;
import com.example.mycourseschedule.DAO.CourseDao;
import com.example.mycourseschedule.DAO.TermDao;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Course;
import com.example.mycourseschedule.Models.Term;
import com.example.mycourseschedule.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CourseListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private CourseDao courseDao;
    private TermDao termDao;
    private int termId;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // Get termId from Intent
        termId = getIntent().getIntExtra("termId", -1);
        if (termId == -1) {
            Toast.makeText(this, "Invalid Term ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize DAOs
        courseDao = AppDatabase.getInstance(this).courseDao();
        termDao = AppDatabase.getInstance(this).termDao();

        // Fetch term title and set ActionBar title
        executor.execute(() -> {
            Term term = termDao.getTermById(termId);
            runOnUiThread(() -> {
                if (term != null) {
                    getSupportActionBar().setTitle("Courses for " + term.getTitle());
                } else {
                    getSupportActionBar().setTitle("Courses for Term " + termId); // Fallback
                }
            });
        });

        recyclerView = findViewById(R.id.course_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load courses for the term
        courseDao.getCoursesForTerm(termId).observe(this, courses -> {
            if (courses != null) {
                adapter.setCourses(courses);
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_course_fab);
        fab.setOnClickListener(v -> showAddCourseDialog());
    }

    private void showAddCourseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_course, null);
        EditText titleInput = dialogView.findViewById(R.id.course_title_input);
        EditText startDateInput = dialogView.findViewById(R.id.course_start_date_input);
        EditText endDateInput = dialogView.findViewById(R.id.course_end_date_input);
        Spinner statusInput = dialogView.findViewById(R.id.course_status_input);
        EditText instructorNameInput = dialogView.findViewById(R.id.instructor_name_input);
        EditText instructorPhoneInput = dialogView.findViewById(R.id.instructor_phone_input);
        EditText instructorEmailInput = dialogView.findViewById(R.id.instructor_email_input);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"in progress", "completed", "dropped", "plan to take"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusInput.setAdapter(statusAdapter);

        Calendar calendar = Calendar.getInstance();
        startDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        startDateInput.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        endDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        endDateInput.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Course")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String startDateStr = startDateInput.getText().toString().trim();
                    String endDateStr = endDateInput.getText().toString().trim();
                    String status = statusInput.getSelectedItem().toString();
                    String instructorName = instructorNameInput.getText().toString().trim();
                    String instructorPhone = instructorPhoneInput.getText().toString().trim();
                    String instructorEmail = instructorEmailInput.getText().toString().trim();

                    if (title.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
                        Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        Date startDate = sdf.parse(startDateStr);
                        Date endDate = sdf.parse(endDateStr);

                        if (endDate.before(startDate)) {
                            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Course course = new Course();
                        course.setTermId(termId);
                        course.setTitle(title);
                        course.setStartDate(startDate);
                        course.setEndDate(endDate);
                        course.setStatus(status);
                        course.setInstructorName(instructorName);
                        course.setInstructorPhone(instructorPhone);
                        course.setInstructorEmail(instructorEmail);

                        executor.execute(() -> courseDao.insert(course));
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}