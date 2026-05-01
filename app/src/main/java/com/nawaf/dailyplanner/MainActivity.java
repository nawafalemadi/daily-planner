package com.nawaf.dailyplanner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nawaf.dailyplanner.data.database.AppDatabase;
import com.nawaf.dailyplanner.data.model.Task;
import com.nawaf.dailyplanner.ui.tasks.TaskAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private TaskAdapter adapter;
    private LiveData<List<Task>> currentLiveData;
    private RecyclerView rvTasks;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        final long startTime = System.currentTimeMillis();

        androidx.core.splashscreen.SplashScreen splashScreen =
                androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        splashScreen.setKeepOnScreenCondition(() ->
                System.currentTimeMillis() - startTime < 2500
        );

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);

        Button btnAddTask = findViewById(R.id.btnAddTask);
        Button btnStatistics = findViewById(R.id.btnStatistics);
        FloatingActionButton btnGuide = findViewById(R.id.btnGuide);
        rvTasks = findViewById(R.id.rvTasks);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        Spinner spinnerSort = findViewById(R.id.spinnerSort);

        adapter = new TaskAdapter(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskChecked(Task task) {
                Executors.newSingleThreadExecutor().execute(() -> db.taskDao().update(task));
            }

            @Override
            public void onTaskLongClick(Task task) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) ->
                                Executors.newSingleThreadExecutor().execute(() -> db.taskDao().delete(task)))
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onTaskClick(Task task) {
                showTaskDetailsDialog(task);
            }
        });

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(adapter);

        String[] sortOptions = {"Sort by Due Date", "Sort by Priority", "Sort by Newest"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        observeTasksBySort(0);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                observeTasksBySort(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnAddTask.setOnClickListener(v -> showAddTaskDialog());

        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        btnGuide.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.guide))
                    .setMessage(getString(R.string.guide_message))
                    .setPositiveButton(getString(R.string.close), null)
                    .show();
        });
    }

    private void observeTasksBySort(int sortOption) {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        if (sortOption == 0) {
            currentLiveData = db.taskDao().getTasksByDueDate();
        } else if (sortOption == 1) {
            currentLiveData = db.taskDao().getTasksByPriority();
        } else {
            currentLiveData = db.taskDao().getTasksByNewest();
        }

        currentLiveData.observe(this, tasks -> {
            adapter.setTasks(tasks);

            if (tasks == null || tasks.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvTasks.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvTasks.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        EditText etDialogTaskTitle = dialogView.findViewById(R.id.etDialogTaskTitle);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnPickTime = dialogView.findViewById(R.id.btnPickTime);
        TextView tvSelectedDateTime = dialogView.findViewById(R.id.tvSelectedDateTime);

        String[] priorities = {"Low", "Medium", "High", "Urgent"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                priorities
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        Calendar selectedDateTime = Calendar.getInstance();
        updateDateTimeText(tvSelectedDateTime, selectedDateTime);

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateTimeText(tvSelectedDateTime, selectedDateTime);
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MainActivity.this,
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        selectedDateTime.set(Calendar.SECOND, 0);
                        updateDateTimeText(tvSelectedDateTime, selectedDateTime);
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etDialogTaskTitle.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long now = System.currentTimeMillis();
                    long due = selectedDateTime.getTimeInMillis();
                    int priority = spinnerPriority.getSelectedItemPosition();

                    Task task = new Task(title, "", due, priority, 0, now);

                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.taskDao().insert(task);
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Task added!", Toast.LENGTH_SHORT).show()
                        );
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTaskDetailsDialog(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task_details, null);

        TextView tvDetailTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvDetailPriority = dialogView.findViewById(R.id.tvDetailPriority);
        TextView tvDetailDueDate = dialogView.findViewById(R.id.tvDetailDueDate);
        TextView tvDetailStatus = dialogView.findViewById(R.id.tvDetailStatus);

        tvDetailTitle.setText(task.getTitle());

        String priorityText;
        switch (task.getPriority()) {
            case 0:
                priorityText = "Low";
                break;
            case 1:
                priorityText = "Medium";
                break;
            case 2:
                priorityText = "High";
                break;
            default:
                priorityText = "Urgent";
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        tvDetailPriority.setText("Priority: " + priorityText);
        tvDetailDueDate.setText("Due date: " + sdf.format(task.getDueAtMillis()));
        tvDetailStatus.setText("Status: " + (task.getStatus() == 1 ? "Completed" : "Not completed"));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Edit Task", (dialog, which) -> showEditTaskDialog(task))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showEditTaskDialog(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        EditText etDialogTaskTitle = dialogView.findViewById(R.id.etDialogTaskTitle);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnPickTime = dialogView.findViewById(R.id.btnPickTime);
        TextView tvSelectedDateTime = dialogView.findViewById(R.id.tvSelectedDateTime);

        String[] priorities = {"Low", "Medium", "High", "Urgent"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                priorities
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        Calendar selectedDateTime = Calendar.getInstance();
        selectedDateTime.setTimeInMillis(task.getDueAtMillis());

        etDialogTaskTitle.setText(task.getTitle());
        spinnerPriority.setSelection(task.getPriority());
        updateDateTimeText(tvSelectedDateTime, selectedDateTime);

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateTimeText(tvSelectedDateTime, selectedDateTime);
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MainActivity.this,
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        selectedDateTime.set(Calendar.SECOND, 0);
                        updateDateTimeText(tvSelectedDateTime, selectedDateTime);
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etDialogTaskTitle.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    task.setTitle(title);
                    task.setPriority(spinnerPriority.getSelectedItemPosition());
                    task.setDueAtMillis(selectedDateTime.getTimeInMillis());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.taskDao().update(task);
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Task updated!", Toast.LENGTH_SHORT).show()
                        );
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDateTimeText(TextView textView, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        textView.setText("Selected: " + sdf.format(calendar.getTime()));
    }
}