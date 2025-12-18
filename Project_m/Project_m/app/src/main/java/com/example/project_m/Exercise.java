package com.example.project_m;

public class Exercise {
    private String id;
    private String title;
    private String description;
    private String instruction;
    private String category;
    private int duration;
    private int progress;
    private boolean completed;

    public Exercise(String id, String title, String description, String instruction, String category, int duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instruction = instruction;
        this.category = category;
        this.duration = duration;
        this.progress = 0;
        this.completed = false;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstruction() { return instruction; }
    public String getCategory() { return category; }
    public int getDuration() { return duration; }
    public int getProgress() { return progress; }
    public boolean isCompleted() { return completed; }

    public void setProgress(int progress) { this.progress = progress; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}