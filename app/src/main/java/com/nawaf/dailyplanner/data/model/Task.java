package com.nawaf.dailyplanner.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;

    private long dueAtMillis;

    // 0=LOW, 1=MEDIUM, 2=HIGH, 3=URGENT
    private int priority;

    // 0=TODO, 1=IN_PROGRESS, 2=DONE
    private int status;

    private long createdAtMillis;

    public Task(String title, String description, long dueAtMillis, int priority, int status, long createdAtMillis) {
        this.title = title;
        this.description = description;
        this.dueAtMillis = dueAtMillis;
        this.priority = priority;
        this.status = status;
        this.createdAtMillis = createdAtMillis;
        this.createdAtMillis = createdAtMillis;
    }

    public Task() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDueAtMillis() { return dueAtMillis; }
    public void setDueAtMillis(long dueAtMillis) { this.dueAtMillis = dueAtMillis; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
}