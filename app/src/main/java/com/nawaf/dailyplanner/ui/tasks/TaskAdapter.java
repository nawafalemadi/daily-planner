package com.nawaf.dailyplanner.ui.tasks;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nawaf.dailyplanner.R;
import com.nawaf.dailyplanner.data.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onTaskChecked(Task task);
        void onTaskLongClick(Task task);
        void onTaskClick(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskActionListener listener;

    public TaskAdapter(OnTaskActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        boolean isCompleted = task.getStatus() == 1;
        boolean isOverdue = !isCompleted && task.getDueAtMillis() < System.currentTimeMillis();

        String dueDateText = sdf.format(task.getDueAtMillis());

        if (isOverdue) {
            holder.tvDueDate.setText(holder.itemView.getContext().getString(
                    R.string.due_overdue_format,
                    dueDateText,
                    holder.itemView.getContext().getString(R.string.overdue)
            ));
            holder.tvDueDate.setTextColor(Color.parseColor("#C62828"));
        } else {
            holder.tvDueDate.setText(holder.itemView.getContext().getString(
                    R.string.due_format,
                    dueDateText
            ));
            holder.tvDueDate.setTextColor(Color.parseColor("#444444"));
        }

        String priorityText;
        int priorityColor;

        switch (task.getPriority()) {
            case 0:
                priorityText = holder.itemView.getContext().getString(R.string.priority_low);
                priorityColor = Color.parseColor("#2E7D32");
                break;
            case 1:
                priorityText = holder.itemView.getContext().getString(R.string.priority_medium);
                priorityColor = Color.parseColor("#1565C0");
                break;
            case 2:
                priorityText = holder.itemView.getContext().getString(R.string.priority_high);
                priorityColor = Color.parseColor("#EF6C00");
                break;
            default:
                priorityText = holder.itemView.getContext().getString(R.string.priority_urgent);
                priorityColor = Color.parseColor("#C62828");
                break;
        }

        holder.tvPriority.setText(holder.itemView.getContext().getString(
                R.string.priority_format,
                priorityText
        ));
        holder.tvPriority.setTextColor(priorityColor);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(isCompleted);

        if (isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? 1 : 0);

            holder.itemView.animate()
                    .alpha(0.5f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        holder.itemView.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();

                        listener.onTaskChecked(task);
                    })
                    .start();
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onTaskLongClick(task);
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvDueDate;
        TextView tvPriority;
        CheckBox checkBox;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            checkBox = itemView.findViewById(R.id.cbDone);
        }
    }
}