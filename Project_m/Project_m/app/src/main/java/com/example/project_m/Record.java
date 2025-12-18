package com.example.project_m;

public class Record {
    private int id;
    private String userId;
    private String note;
    private String accuracy;
    private String frequency;
    private String duration;
    private String timestamp;

    // Конструктор
    public Record(int id, String userId, String note, String accuracy,
                  String frequency, String duration, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.note = note;
        this.accuracy = accuracy;
        this.frequency = frequency;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    // Геттеры
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getNote() { return note; }
    public String getAccuracy() { return accuracy; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public String getTimestamp() { return timestamp; }

    // Сеттеры (если нужны)
    public void setId(int id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setNote(String note) { this.note = note; }
    public void setAccuracy(String accuracy) { this.accuracy = accuracy; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}