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

import com.example.mycourseschedule.Activity.AssessmentDetailActivity;
import com.example.mycourseschedule.Activity.AssessmentListActivity;
import com.example.mycourseschedule.DAO.AssessmentDao;
import com.example.mycourseschedule.Helper.AppDatabase;
import com.example.mycourseschedule.Models.Assessment;
import com.example.mycourseschedule.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.ViewHolder> {
    private List<Assessment> assessments = new ArrayList<>();
    private Context context;
    private AssessmentDao assessmentDao;
    private Executor executor = Executors.newSingleThreadExecutor();

    public AssessmentAdapter(Context context) {
        this.context = context;
        this.assessmentDao = AppDatabase.getInstance(context).assessmentDao();
    }

    public void setAssessments(List<Assessment> assessments) {
        this.assessments = assessments != null ? assessments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assessment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Assessment assessment = assessments.get(position);
        holder.titleTextView.setText(assessment.getTitle() != null ? assessment.getTitle() : "No Title");
        holder.typeTextView.setText(assessment.getType() != null ? assessment.getType() : "No Type");

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        holder.startDateTextView.setText(assessment.getStartDate() != null ? sdf.format(assessment.getStartDate()) : "No Start Date");
        holder.endDateTextView.setText(assessment.getEndDate() != null ? sdf.format(assessment.getEndDate()) : "No End Date");

        // Handle click on the assessment item to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AssessmentDetailActivity.class);
            intent.putExtra("assessmentId", assessment.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Assessment")
                    .setMessage("Are you sure you want to delete " + assessment.getTitle() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        executor.execute(() -> {
                            assessmentDao.delete(assessment);
                            ((AssessmentListActivity) context).runOnUiThread(() -> {
                                assessments.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Assessment deleted", Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return assessments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView typeTextView;
        TextView startDateTextView;
        TextView endDateTextView;
        Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.assessment_title);
            typeTextView = itemView.findViewById(R.id.assessment_type);
            startDateTextView = itemView.findViewById(R.id.assessment_start_date);
            endDateTextView = itemView.findViewById(R.id.assessment_end_date);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}