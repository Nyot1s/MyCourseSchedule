package com.example.mycourseschedule.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycourseschedule.DAO.TermDao;
import com.example.mycourseschedule.Models.Term;
import com.example.mycourseschedule.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//this is an adapter to show the data from the database in a specified way in my terms list view etc.
public class TermAdapter extends RecyclerView.Adapter<TermAdapter.ViewHolder>   {

    private List<Term> terms = new ArrayList<>();

    private OnTermInteractionListener listener;

    private TermDao termDao;



    public interface OnTermInteractionListener {
        void onViewCourses(int termId);
        void onDeleteTerm(Term term);
    }

    public TermAdapter(OnTermInteractionListener listener, TermDao termDao) {
        this.listener = listener;
        this.termDao = termDao;
    }

    public void setTerms(List<Term> terms) {
    this.terms = terms != null ? terms : new ArrayList<>();
    notifyDataSetChanged();
    }

    //this method sets up the recycle views.
    @NonNull
    @Override
    public TermAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_term, parent, false);
        return new ViewHolder(view);
    }

    //this inputs data into the recycle view
    @Override
    public void onBindViewHolder(@NonNull TermAdapter.ViewHolder holder, int position) {
        Term term = terms.get(position);
        holder.titleTextView.setText(term.getTitle());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM-dd-yyyy", Locale.US);
        holder.datesTextView.setText(sdf.format(term.getStartDate()) + " - " + sdf.format(term.getEndDate()));
        holder.viewCoursesButton.setOnClickListener(v -> listener.onViewCourses(term.getId()));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteTerm(term));

    }

    @Override
    public int getItemCount() {
        return terms.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView datesTextView;
        Button viewCoursesButton;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.term_title);
            datesTextView = itemView.findViewById(R.id.term_dates);
            viewCoursesButton = itemView.findViewById(R.id.view_courses_button);
            deleteButton = itemView.findViewById(R.id.delete_term_button);
        }
    }
}
