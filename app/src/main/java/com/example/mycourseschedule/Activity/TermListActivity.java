package com.example.mycourseschedule.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycourseschedule.Activity.TermDetailActivity;
import com.example.mycourseschedule.Adapter.TermAdapter;
import com.example.mycourseschedule.DAO.TermDao;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Term;
import com.example.mycourseschedule.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TermListActivity extends AppCompatActivity implements TermAdapter.OnTermInteractionListener {
    private TermDao termDao;
    private TermAdapter adapter;

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_list);

        try {
            termDao = AppDatabase.getInstance(this).termDao();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize database", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.activity_term_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TermAdapter(this, termDao);
        recyclerView.setAdapter(adapter);

        termDao.getAllTerms().observe(this, terms -> {
            if (terms != null) {
                adapter.setTerms(terms);
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_term_fab);
        fab.setOnClickListener(view -> showAddTermDialog());
    }

    private void showAddTermDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_term, null);
        EditText titleInput = dialogView.findViewById(R.id.title_input);
        EditText startDateInput = dialogView.findViewById(R.id.start_date_input);
        EditText endDateInput = dialogView.findViewById(R.id.end_date_input);

        //create a calendar instance
        Calendar calendar = Calendar.getInstance();

        //setup a datepicker for the start date
        startDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy", Locale.US);
                        startDateInput.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });
        //setup a datepicker for the end date
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
                .setTitle("Add Term")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String startDateStr = startDateInput.getText().toString().trim();
                    String endDateStr = endDateInput.getText().toString().trim();
                    if(title.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy", Locale.US);
                        Date startDate = sdf.parse(startDateStr);
                        Date endDate = sdf.parse(endDateStr);
                        //verify data input
                        if (endDate.before(startDate)) {
                            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                        }
                        Term term = new Term(title, startDate, endDate);
                        executor.execute(() -> termDao.insert(term));
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onViewCourses(int termId) {
        Intent intent = new Intent(this, CourseListActivity.class);
        intent.putExtra("termId", termId);
        startActivity(intent);
    }

    @Override
    public void onDeleteTerm(Term term) {
        executor.execute(() -> {
            int courseCount = termDao.getCourseCount(term.getId());
            runOnUiThread(() -> {
                if (courseCount > 0) {
                    Toast.makeText(this, "Cannot delete term with courses", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Term")
                            .setMessage("Are you sure you want to delete " + term.getTitle() + "?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                executor.execute(() -> termDao.delete(term));
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        });

    }

    public void onItemClick(Term term) {
        // Handle item click, e.g., navigate to TermDetailActivity
        Intent intent = new Intent(this, TermDetailActivity.class);
        intent.putExtra("termId", term.getId());
        startActivity(intent);
    }


    public void onDeleteClick(Term term) {
        // Handle delete click, e.g., remove the term from the database
        new Thread(() -> {
            termDao.delete(term);
            runOnUiThread(() -> Toast.makeText(this, "Term deleted", Toast.LENGTH_SHORT).show());
        }).start();
    }
}