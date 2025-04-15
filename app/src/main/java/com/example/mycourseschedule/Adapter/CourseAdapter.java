package com.example.mycourseschedule.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycourseschedule.Activity.CourseDetailActivity;
import com.example.mycourseschedule.Activity.CourseListActivity;
import com.example.mycourseschedule.DAO.AssessmentDao;
import com.example.mycourseschedule.DAO.CourseDao;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Course;
import com.example.mycourseschedule.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> courses = new ArrayList<>();
    private Context context;
    private CourseDao courseDao;
    private AssessmentDao assessmentDao;
    private Executor executor = Executors.newSingleThreadExecutor();

    public CourseAdapter(Context context) {
        this.context = context;
        this.courseDao = AppDatabase.getInstance(context).courseDao();
        this.assessmentDao = AppDatabase.getInstance(context).assessmentDao();
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.titleTextView.setText(course.getTitle() != null ? course.getTitle() : "No Title");

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        holder.startDateTextView.setText(course.getStartDate() != null ? sdf.format(course.getStartDate()) : "No Start Date");
        holder.endDateTextView.setText(course.getEndDate() != null ? sdf.format(course.getEndDate()) : "No End Date");
        holder.statusTextView.setText(course.getStatus() != null ? course.getStatus() : "No Status");

        // Handle click on the course item to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CourseDetailActivity.class);
            intent.putExtra("courseId", course.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            executor.execute(() -> {
                int assessmentCount = assessmentDao.getAssessmentCount(course.getId());
                ((CourseListActivity) context).runOnUiThread(() -> {
                    if (assessmentCount > 0) {
                        Toast.makeText(context, "Cannot delete course with assessments", Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete Course")
                                .setMessage("Are you sure you want to delete " + course.getTitle() + "?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    executor.execute(() -> {
                                        courseDao.delete(course);
                                        ((CourseListActivity) context).runOnUiThread(() -> {
                                            courses.remove(position);
                                            notifyItemRemoved(position);
                                            Toast.makeText(context, "Course deleted", Toast.LENGTH_SHORT).show();
                                        });
                                    });
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
            });
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView statusTextView;
        TextView startDateTextView;
        TextView endDateTextView;
        Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.course_title);
            startDateTextView = itemView.findViewById(R.id.course_start_date);
            endDateTextView = itemView.findViewById(R.id.course_end_date);
            statusTextView = itemView.findViewById(R.id.course_status);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}