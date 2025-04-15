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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mycourseschedule.DAO.AssessmentDao;
import com.example.mycourseschedule.Helper.AlertReceiver;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Assessment;
import com.example.mycourseschedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AssessmentDetailActivity extends AppCompatActivity {
    private EditText titleInput, startDateInput, endDateInput;
    private Spinner typeInput;
    private Button saveButton, deleteButton;
    private AssessmentDao assessmentDao;
    private Assessment assessment;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int assessmentId;
    private String originalStartDateStr, originalEndDateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);

        // Initialize DAO
        assessmentDao = AppDatabase.getInstance(this).assessmentDao();

        // Get assessmentId from Intent
        assessmentId = getIntent().getIntExtra("assessmentId", -1);
        if (assessmentId == -1) {
            Toast.makeText(this, "Invalid Assessment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        titleInput = findViewById(R.id.assessment_title_input);
        typeInput = findViewById(R.id.assessment_type_input);
        startDateInput = findViewById(R.id.assessment_start_date_input);
        endDateInput = findViewById(R.id.assessment_end_date_input);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        // Set up type spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"performance", "objective"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeInput.setAdapter(typeAdapter);

        // Load assessment details
        executor.execute(() -> {
            assessment = assessmentDao.getAssessmentById(assessmentId);
            runOnUiThread(() -> {
                if (assessment != null) {
                    getSupportActionBar().setTitle(assessment.getTitle());
                    titleInput.setText(assessment.getTitle());
                    typeInput.setSelection(typeAdapter.getPosition(assessment.getType()));
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    originalStartDateStr = sdf.format(assessment.getStartDate());
                    originalEndDateStr = sdf.format(assessment.getEndDate());
                    startDateInput.setText(originalStartDateStr);
                    endDateInput.setText(originalEndDateStr);
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
        saveButton.setOnClickListener(v -> saveAssessment());

        // Delete button
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Assessment")
                    .setMessage("Are you sure you want to delete this assessment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        executor.execute(() -> {
                            assessmentDao.delete(assessment);
                            runOnUiThread(() -> finish());
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void saveAssessment() {
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
            Date startDate;
            Date endDate;

            // Check if start date has changed; if not, use the original date
            if (startDateStr.equals(originalStartDateStr)) {
                startDate = assessment.getStartDate();
            } else {
                startDate = sdf.parse(startDateStr);
            }

            // Check if end date has changed; if not, use the original date
            if (endDateStr.equals(originalEndDateStr)) {
                endDate = assessment.getEndDate();
            } else {
                endDate = sdf.parse(endDateStr);
            }

            if (endDate.before(startDate)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update assessment details
            assessment.setTitle(title);
            assessment.setType(type);
            assessment.setStartDate(startDate);
            assessment.setEndDate(endDate);

            // Set alerts for start and end dates
            setAlert(startDate, "Assessment " + title + " starts today!");
            setAlert(endDate, "Assessment " + title + " ends today!");

            // Save assessment to database
            executor.execute(() -> {
                try {
                    assessmentDao.update(assessment);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Assessment updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    Log.e("AssessmentDetailActivity", "Error saving assessment: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(this, "Error saving assessment", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (ParseException e) {
            Log.e("AssessmentDetailActivity", "Date parse error: " + e.getMessage());
            Toast.makeText(this, "Invalid date format: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AssessmentDetailActivity", "Unexpected error: " + e.getMessage());
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