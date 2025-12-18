package com.example.project_m;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 2; // Увеличиваем версию
    private static final String DATABASE_NAME = "VocalTuner.db";

    private static final String TABLE_RECORDS = "records";

    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_NOTE = "note";
    private static final String KEY_ACCURACY = "accuracy";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_AUDIO_PATH = "audio_path"; // Новое поле

    private static final String CREATE_TABLE_RECORDS = "CREATE TABLE " + TABLE_RECORDS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " TEXT,"
            + KEY_NOTE + " TEXT,"
            + KEY_ACCURACY + " TEXT,"
            + KEY_FREQUENCY + " TEXT,"
            + KEY_DURATION + " TEXT,"
            + KEY_TIMESTAMP + " TEXT,"
            + KEY_AUDIO_PATH + " TEXT," // Добавляем путь к аудиофайлу
            + KEY_CREATED_AT + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECORDS);
        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Добавляем поле audio_path для существующей таблицы
            db.execSQL("ALTER TABLE " + TABLE_RECORDS + " ADD COLUMN " + KEY_AUDIO_PATH + " TEXT");
            Log.d(TAG, "Database upgraded to version 2");
        }
    }

    public static class Record {
        private int id;
        private String userId;
        private String note;
        private String accuracy;
        private String frequency;
        private String duration;
        private String timestamp;
        private String audioPath; // Новое поле

        public Record(int id, String userId, String note, String accuracy,
                      String frequency, String duration, String timestamp, String audioPath) {
            this.id = id;
            this.userId = userId;
            this.note = note;
            this.accuracy = accuracy;
            this.frequency = frequency;
            this.duration = duration;
            this.timestamp = timestamp;
            this.audioPath = audioPath;
        }

        public int getId() { return id; }
        public String getUserId() { return userId; }
        public String getNote() { return note; }
        public String getAccuracy() { return accuracy; }
        public String getFrequency() { return frequency; }
        public String getDuration() { return duration; }
        public String getTimestamp() { return timestamp; }
        public String getAudioPath() { return audioPath; } // Новый геттер

        public void setId(int id) { this.id = id; }
        public void setUserId(String userId) { this.userId = userId; }
        public void setNote(String note) { this.note = note; }
        public void setAccuracy(String accuracy) { this.accuracy = accuracy; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public void setDuration(String duration) { this.duration = duration; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

        // Проверка существования аудиофайла
        public boolean hasAudioFile() {
            return audioPath != null && !audioPath.isEmpty();
        }
    }

    // Обновленный метод с аудиопутём
    public boolean addRecord(String userId, String note, String accuracy,
                             String frequency, String duration, String timestamp, String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userId);
        values.put(KEY_NOTE, note);
        values.put(KEY_ACCURACY, accuracy);
        values.put(KEY_FREQUENCY, frequency);
        values.put(KEY_DURATION, duration);
        values.put(KEY_TIMESTAMP, timestamp);
        values.put(KEY_AUDIO_PATH, audioPath); // Сохраняем путь
        values.put(KEY_CREATED_AT, System.currentTimeMillis());

        long result = db.insert(TABLE_RECORDS, null, values);
        db.close();

        boolean success = result != -1;
        Log.d(TAG, "Record added for user " + userId + ": " + (success ? "SUCCESS" : "FAILED"));
        Log.d(TAG, "Audio path: " + audioPath);
        return success;
    }

    // Метод без аудиопутём (для обратной совместимости)
    public boolean addRecord(String userId, String note, String accuracy,
                             String frequency, String duration, String timestamp) {
        return addRecord(userId, note, accuracy, frequency, duration, timestamp, "");
    }

    public List<Record> getUserRecords(String userId) {
        List<Record> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Log.d(TAG, "Getting records for user: " + userId);

        String query = "SELECT * FROM " + TABLE_RECORDS + " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_CREATED_AT + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        Log.d(TAG, "Records found in DB: " + cursor.getCount());

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    Record record = new Record(
                            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_ACCURACY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_FREQUENCY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_DURATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_AUDIO_PATH)) // Получаем путь
                    );
                    records.add(record);
                    Log.d(TAG, "Record loaded: " + record.getNote() + ", Audio: " + record.getAudioPath());
                } catch (Exception e) {
                    Log.e(TAG, "Error creating Record object: " + e.getMessage());
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.d(TAG, "No records found or cursor is empty");
        }
        db.close();

        Log.d(TAG, "Retrieved " + records.size() + " records for user: " + userId);
        return records;
    }

    public boolean deleteUserRecords(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_RECORDS, KEY_USER_ID + " = ?", new String[]{userId});
        db.close();

        Log.d(TAG, "Deleted " + rowsDeleted + " records for user: " + userId);
        return rowsDeleted > 0;
    }

    @Override
    public synchronized void close() {
        super.close();
        Log.d(TAG, "Database closed");
    }
}