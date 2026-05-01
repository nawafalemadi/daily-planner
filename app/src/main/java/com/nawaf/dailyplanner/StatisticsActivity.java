package com.nawaf.dailyplanner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nawaf.dailyplanner.data.database.AppDatabase;

public class StatisticsActivity extends AppCompatActivity {

    private AppDatabase db;

    private TextView tvTotal;
    private TextView tvCompleted;
    private TextView tvPending;
    private TextView tvOverdue;
    private TextView tvCompletionRate;
    private TextView tvSummary;

    private ProgressBar progressOverall;
    private ProgressBar progressCompleted;
    private ProgressBar progressPending;
    private ProgressBar progressOverdue;

    private int totalTasks = 0;
    private int completedTasks = 0;
    private int pendingTasks = 0;
    private int overdueTasks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);

        tvTotal = findViewById(R.id.tvTotal);
        tvCompleted = findViewById(R.id.tvCompleted);
        tvPending = findViewById(R.id.tvPending);
        tvOverdue = findViewById(R.id.tvOverdue);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvSummary = findViewById(R.id.tvSummary);

        progressOverall = findViewById(R.id.progressOverall);
        progressCompleted = findViewById(R.id.progressCompleted);
        progressPending = findViewById(R.id.progressPending);
        progressOverdue = findViewById(R.id.progressOverdue);

        Button btnBack = findViewById(R.id.btnBack);

        db.taskDao().getTotalTasks().observe(this, count -> {
            totalTasks = count != null ? count : 0;
            updateStatisticsUI();
        });

        db.taskDao().getCompletedTasks().observe(this, count -> {
            completedTasks = count != null ? count : 0;
            updateStatisticsUI();
        });

        db.taskDao().getPendingTasks().observe(this, count -> {
            pendingTasks = count != null ? count : 0;
            updateStatisticsUI();
        });

        db.taskDao().getOverdueTasks(System.currentTimeMillis()).observe(this, count -> {
            overdueTasks = count != null ? count : 0;
            updateStatisticsUI();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateStatisticsUI() {
        int completionPercent = 0;
        int pendingPercent = 0;
        int overduePercent = 0;

        if (totalTasks > 0) {
            completionPercent = (completedTasks * 100) / totalTasks;
            pendingPercent = (pendingTasks * 100) / totalTasks;
            overduePercent = (overdueTasks * 100) / totalTasks;
        }

        tvTotal.setText(getString(R.string.total_tasks_format, totalTasks));
        tvCompleted.setText(getString(R.string.completed_tasks_format, completedTasks));
        tvPending.setText(getString(R.string.pending_tasks_format, pendingTasks));
        tvOverdue.setText(getString(R.string.overdue_tasks_format, overdueTasks));

        tvCompletionRate.setText(getString(R.string.completed_format, completionPercent));
        tvSummary.setText(getString(R.string.tasks_completed_summary, completedTasks, totalTasks));

        progressOverall.setProgress(completionPercent);
        progressCompleted.setProgress(completionPercent);
        progressPending.setProgress(pendingPercent);
        progressOverdue.setProgress(overduePercent);
    }
}