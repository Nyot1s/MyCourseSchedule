package com.example.mycourseschedule.Activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mycourseschedule.DAO.CourseDao;
import com.example.mycourseschedule.DAO.NoteDao;
import com.example.mycourseschedule.Helper.AlertReceiver;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Course;
import com.example.mycourseschedule.Models.Note;
import com.example.mycourseschedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CourseDetailActivity extends AppCompatActivity {
    private EditText titleInput, startDateInput, endDateInput, instructorNameInput, instructorPhoneInput, instructorEmailInput, noteInput;
    private Spinner statusInput;
    private Button saveButton, deleteButton, shareNoteButton, viewAssessmentsButton;
    private CourseDao courseDao;
    private NoteDao noteDao;
    private Course course;
    private Note note;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int courseId;
    private String originalStartDateStr, originalEndDateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Initialize DAOs
        courseDao = AppDatabase.getInstance(this).courseDao();
        noteDao = AppDatabase.getInstance(this).noteDao();

        // Get courseId from Intent
        courseId = getIntent().getIntExtra("courseId", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Invalid Course ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        titleInput = findViewById(R.id.course_title_input);
        startDateInput = findViewById(R.id.course_start_date_input);
        endDateInput = findViewById(R.id.course_end_date_input);
        statusInput = findViewById(R.id.course_status_input);
        instructorNameInput = findViewById(R.id.instructor_name_input);
        instructorPhoneInput = findViewById(R.id.instructor_phone_input);
        instructorEmailInput = findViewById(R.id.instructor_email_input);
        noteInput = findViewById(R.id.note_input);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        shareNoteButton = findViewById(R.id.share_note_button);
        viewAssessmentsButton = findViewById(R.id.view_assessments_button);

        // Set up status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"in progress", "completed", "dropped", "plan to take"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusInput.setAdapter(statusAdapter);

        // Load course details
        executor.execute(() -> {
            course = courseDao.getCourseById(courseId);
            note = noteDao.getNoteByCourseId(courseId);
            runOnUiThread(() -> {
                if (course != null) {
                    getSupportActionBar().setTitle(course.getTitle());
                    titleInput.setText(course.getTitle());
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    //save original dates
                    originalStartDateStr = sdf.format(course.getStartDate());
                    originalEndDateStr = sdf.format(course.getEndDate());

                    //set the dates to original variables to be modified
                    startDateInput.setText(originalStartDateStr);
                    endDateInput.setText(originalEndDateStr);

                    statusInput.setSelection(statusAdapter.getPosition(course.getStatus()));
                    instructorNameInput.setText(course.getInstructorName());
                    instructorPhoneInput.setText(course.getInstructorPhone());
                    instructorEmailInput.setText(course.getInstructorEmail());
                    if (note != null) {
                        noteInput.setText(note.getContent());
                    }
                }
            });
        });

        // Date pickers
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

        // Save button
        saveButton.setOnClickListener(v -> saveCourse());

        // Delete button
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Course")
                    .setMessage("Are you sure you want to delete this course?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        executor.execute(() -> {
                            courseDao.delete(course);
                            if (note != null) noteDao.delete(note);
                            runOnUiThread(() -> finish());
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Share note button
        shareNoteButton.setOnClickListener(v -> {
            String noteText = noteInput.getText().toString().trim();
            if (!noteText.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, noteText);
                startActivity(Intent.createChooser(shareIntent, "Share note via"));
            } else {
                Toast.makeText(this, "No note to share", Toast.LENGTH_SHORT).show();
            }
        });

        // View assessments button
        viewAssessmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssessmentListActivity.class);
            intent.putExtra("courseId", courseId);
            startActivity(intent);
        });
    }

    private void saveCourse() {
        String title = titleInput.getText().toString().trim();
        String startDateStr = startDateInput.getText().toString().trim();
        String endDateStr = endDateInput.getText().toString().trim();
        String status = statusInput.getSelectedItem().toString();
        String instructorName = instructorNameInput.getText().toString().trim();
        String instructorPhone = instructorPhoneInput.getText().toString().trim();
        String instructorEmail = instructorEmailInput.getText().toString().trim();
        String noteText = noteInput.getText().toString().trim();

        if (title.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date startDate;
            Date endDate;

            // Check if start date has changed; if not, use the original date
            if (startDateStr.equals(originalStartDateStr)) {
                startDate = course.getStartDate();
            } else {
                startDate = sdf.parse(startDateStr);
            }

            // Check if end date has changed; if not, use the original date
            if (endDateStr.equals(originalEndDateStr)) {
                endDate = course.getEndDate();
            } else {
                endDate = sdf.parse(endDateStr);
            }

            if (endDate.before(startDate)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            course.setTitle(title);
            course.setStartDate(startDate);
            course.setEndDate(endDate);
            course.setStatus(status);
            course.setInstructorName(instructorName);
            course.setInstructorPhone(instructorPhone);
            course.setInstructorEmail(instructorEmail);

            // Save note
            if (!noteText.isEmpty()) {
                if (note == null) {
                    note = new Note(courseId, noteText);
                } else {
                    note.setContent(noteText);
                }
            } else if (note != null) {
                executor.execute(() -> {
                    noteDao.delete(note);
                });
                note = null;
            }

            // Set alerts for start and end dates
            setAlert(startDate, "Course " + title + " starts today!");
            setAlert(endDate, "Course " + title + " ends today!");

            // Save course and note to database
            executor.execute(() -> {
                try {
                    courseDao.update(course);
                    if (note != null) {
                        if (note.getId() == 0) { // New note, use insert
                            noteDao.insert(note);
                        } else { // Existing note, use update
                            noteDao.update(note);
                        }
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    Log.e("CourseDetailActivity", "Error saving course: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(this, "Error saving course", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (ParseException e) {
            Log.e("CourseDetailActivity", "Date parse error: " + e.getMessage());
            Toast.makeText(this, "Invalid date format: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("CourseDetailActivity", "Unexpected error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlert(Date date, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) date.getTime(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
    }
}