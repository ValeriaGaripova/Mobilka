package com.example.project_m;

public class ExerciseProgress {
    private String exerciseId;
    private int progress;
    private boolean completed;
    private long lastPracticed;

    public ExerciseProgress(int progress, boolean completed, long lastPracticed) {
        this.progress = progress;
        this.completed = completed;
        this.lastPracticed = lastPracticed;
    }

    // Геттеры и сеттеры
    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public long getLastPracticed() { return lastPracticed; }
    public void setLastPracticed(long lastPracticed) { this.lastPracticed = lastPracticed; }
}