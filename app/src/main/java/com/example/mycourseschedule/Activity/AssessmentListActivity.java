package com.example.mycourseschedule.Activity;

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

import com.example.mycourseschedule.Adapter.AssessmentAdapter;
import com.example.mycourseschedule.DAO.AssessmentDao;
import com.example.mycourseschedule.DAO.CourseDao;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Assessment;
import com.example.mycourseschedule.Models.Course;
import com.example.mycourseschedule.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AssessmentListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AssessmentAdapter adapter;
    private AssessmentDao assessmentDao;
    private CourseDao courseDao;
    private int courseId;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_list);

        // Get courseId from Intent
        courseId = getIntent().getIntExtra("courseId", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Invalid Course ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize DAOs
        assessmentDao = AppDatabase.getInstance(this).assessmentDao();
        courseDao = AppDatabase.getInstance(this).courseDao();

        // Fetch course title and set ActionBar title
        executor.execute(() -> {
            Course course = courseDao.getCourseById(courseId);
            runOnUiThread(() -> {
                if (course != null) {
                    getSupportActionBar().setTitle("Assessments for " + course.getTitle());
                } else {
                    getSupportActionBar().setTitle("Assessments for Course " + courseId);
                }
            });
        });

        recyclerView = findViewById(R.id.assessment_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssessmentAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load assessments for the course
        assessmentDao.getAssessmentsForCourse(courseId).observe(this, assessments -> {
            if (assessments != null) {
                adapter.setAssessments(assessments);
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_assessment_fab);
        fab.setOnClickListener(v -> showAddAssessmentDialog());
    }

    private void showAddAssessmentDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_assessment, null);
        EditText titleInput = dialogView.findViewById(R.id.assessment_title_input);
        Spinner typeInput = dialogView.findViewById(R.id.assessment_type_input);
        EditText startDateInput = dialogView.findViewById(R.id.assessment_start_date_input);
        EditText endDateInput = dialogView.findViewById(R.id.assessment_end_date_input);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"performance", "objective"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeInput.setAdapter(typeAdapter);

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

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Assessment")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String type = typeInput.getSelectedItem().toString();
                    String startDateStr = startDateInput.getText().toString().trim();
                    String endDateStr = endDateInput.getText().toString().trim();

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

                        Assessment assessment = new Assessment(courseId, title, type, startDate, endDate);
                        executor.execute(() -> assessmentDao.insert(assessment));
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}