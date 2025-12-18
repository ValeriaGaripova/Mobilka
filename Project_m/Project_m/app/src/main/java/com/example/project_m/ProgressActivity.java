package com.example.project_m;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProgressActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;

    private TextView tvTotalExercises, tvTotalTime, tvStreak;
    private TextView tvAvgAccuracy, tvTotalRecords;
    private ProgressBar progressAccuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);

        initializeViews();
        loadProgressData();
        setupBackButton();
    }

    private void initializeViews() {
        tvTotalExercises = findViewById(R.id.tvTotalExercises);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvStreak = findViewById(R.id.tvStreak);
        tvAvgAccuracy = findViewById(R.id.tvAvgAccuracy);
        tvTotalRecords = findViewById(R.id.tvTotalRecords);
        progressAccuracy = findViewById(R.id.progressAccuracy);
    }

    private void setupBackButton() {
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProgressData() {
        String currentUserEmail = sharedPreferences.getString("current_user", "");

        // 1. Общая статистика упражнений
        int totalExercises = calculateTotalExercises(currentUserEmail);
        int totalTime = calculateTotalTime(currentUserEmail);
        int streak = calculateStreak(currentUserEmail);

        tvTotalExercises.setText(Integer.toString(totalExercises));
        tvTotalTime.setText(formatTime(totalTime));
        tvStreak.setText(streak + " дней");

        // 2. Прогресс по вокалу (из тюнера)
        double avgAccuracy = calculateAverageAccuracy(currentUserEmail);
        int totalRecords = getTotalRecordsCount(currentUserEmail);

        String accuracyText = String.format(Locale.getDefault(), "%d%%", (int) avgAccuracy);
        tvAvgAccuracy.setText(accuracyText);
        tvTotalRecords.setText(Integer.toString(totalRecords));
        progressAccuracy.setProgress((int) avgAccuracy);
    }

    private int calculateTotalExercises(String userEmail) {
        int total = 0;
        String[] exerciseIds = {"warmup_1", "warmup_2", "range_1", "range_2", "accuracy_1", "accuracy_2"};

        for (String exerciseId : exerciseIds) {
            boolean completed = sharedPreferences.getBoolean(
                    "exercise_" + userEmail + "_" + exerciseId + "_completed", false);
            if (completed) {
                total++;
            }
        }

        return total;
    }

    private int calculateTotalTime(String userEmail) {
        int totalSeconds = 0;
        String[] exerciseIds = {"warmup_1", "warmup_2", "range_1", "range_2", "accuracy_1", "accuracy_2"};

        for (String exerciseId : exerciseIds) {
            int progress = sharedPreferences.getInt(
                    "exercise_" + userEmail + "_" + exerciseId + "_progress", 0);

            int duration = getExerciseDuration(exerciseId);
            int actualTime = (int) (duration * (progress / 100.0));
            totalSeconds += actualTime;
        }

        return totalSeconds;
    }

    private int getExerciseDuration(String exerciseId) {
        int duration = 0;
        if ("warmup_1".equals(exerciseId)) {
            duration = 300;
        } else if ("warmup_2".equals(exerciseId)) {
            duration = 180;
        } else if ("range_1".equals(exerciseId)) {
            duration = 240;
        } else if ("range_2".equals(exerciseId)) {
            duration = 180;
        } else if ("accuracy_1".equals(exerciseId)) {
            duration = 300;
        } else if ("accuracy_2".equals(exerciseId)) {
            duration = 240;
        }
        return duration;
    }

    private int calculateStreak(String userEmail) {
        Set<String> datesSet = sharedPreferences.getStringSet(
                "exercise_dates_" + userEmail, new HashSet<>());

        if (datesSet.isEmpty()) return 0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        int streak = 0;
        boolean foundToday = false;

        if (datesSet.contains(today)) {
            streak = 1;
            foundToday = true;
        }

        if (!foundToday) return 0;

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        for (int i = 0; i < 30; i++) {
            String date = sdf.format(calendar.getTime());
            if (datesSet.contains(date)) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    private double calculateAverageAccuracy(String userEmail) {
        List<DatabaseHelper.Record> records = dbHelper.getUserRecords(userEmail);
        if (records == null || records.isEmpty()) return 0.0;

        double totalAccuracy = 0;
        int count = 0;

        for (DatabaseHelper.Record record : records) {
            // ВАЖНО: Проверьте, что возвращает getAccuracy()
            // Если int:
            // int accuracy = record.getAccuracy();

            // Если String (более вероятно, т.к. в тюнере точность "%"):
            String accuracyStr = record.getAccuracy().replace("%", "").trim();

            try {
                int accuracy = Integer.parseInt(accuracyStr);
                if (accuracy > 0) {
                    totalAccuracy += accuracy;
                    count++;
                }
            } catch (NumberFormatException e) {
                // Пропускаем нечисловые значения
                continue;
            }
        }

        return count > 0 ? totalAccuracy / count : 0.0;
    }

    private int getTotalRecordsCount(String userEmail) {
        List<DatabaseHelper.Record> records = dbHelper.getUserRecords(userEmail);
        return records != null ? records.size() : 0;
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + " сек";
        } else if (totalSeconds < 3600) {
            int minutes = totalSeconds / 60;
            return minutes + " мин";
        } else {
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            return String.format(Locale.getDefault(), "%dч %02dмин", hours, minutes);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProgressData();
    }
}