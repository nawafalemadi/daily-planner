package com.nawaf.dailyplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nawaf.dailyplanner.data.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY dueAtMillis ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks ORDER BY dueAtMillis ASC")
    LiveData<List<Task>> getTasksByDueDate();

    @Query("SELECT * FROM tasks ORDER BY priority DESC")
    LiveData<List<Task>> getTasksByPriority();

    @Query("SELECT * FROM tasks ORDER BY createdAtMillis DESC")
    LiveData<List<Task>> getTasksByNewest();

    @Query("SELECT COUNT(*) FROM tasks")
    LiveData<Integer> getTotalTasks();

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 1")
    LiveData<Integer> getCompletedTasks();

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 0")
    LiveData<Integer> getPendingTasks();

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 0 AND dueAtMillis < :currentTime")
    LiveData<Integer> getOverdueTasks(long currentTime);
}
