package com.example.project_m;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUsername, tvUserStats, btnEditName;
    private Button btnTuner, btnCalibrate, btnExercises, btnProgress, btnLogout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        checkLoginStatus();
        setupClickListeners();
        loadUserData();
    }

    private void initializeViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvUserStats = findViewById(R.id.tvUserStats);
        btnEditName = findViewById(R.id.btnEditName);

        btnTuner = findViewById(R.id.btnTuner);
        btnCalibrate = findViewById(R.id.btnCalibrate);
        btnExercises = findViewById(R.id.btnExercises);
        btnProgress = findViewById(R.id.btnProgress);
        btnLogout = findViewById(R.id.btnLogout);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
    }

    private void checkLoginStatus() {
        boolean isLoggedIn = sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);

        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadUserData() {
        String currentUser = sharedPreferences.getString(Constants.KEY_CURRENT_USER, "Пользователь");
        tvUsername.setText("👤 " + currentUser);

        updateUserStats();
    }

    private void updateUserStats() {
        // Здесь можно добавить реальную статистику позже
        String stats = "🎤 Твои музыкальные приключения начинаются!";
        tvUserStats.setText(stats);
    }

    private void setupClickListeners() {
        // Кнопка редактирования имени
        btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNameDialog();
            }
        });

        // ТЮНЕР - ГЛАВНАЯ КНОПКА
        btnTuner.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(DashboardActivity.this, TunerActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(DashboardActivity.this, "Ошибка открытия тюнера", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        btnCalibrate.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(DashboardActivity.this, CalibrationActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(DashboardActivity.this, "Ошибка открытия калибровки", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        btnExercises.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(DashboardActivity.this, ExercisesActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(DashboardActivity.this, "Упражнения скоро будут доступны", Toast.LENGTH_SHORT).show();
            }
        });

        btnProgress.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(DashboardActivity.this, ProgressActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(DashboardActivity.this, "Прогресс скоро будет доступен", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Изменить имя");
        builder.setMessage("Введите новое имя:");

        // Создаем EditText для ввода
        final EditText input = new EditText(this);

        // Получаем текущее имя без эмодзи
        String currentName = getCurrentUserName();
        input.setText(currentName);
        input.setSelectAllOnFocus(true);

        builder.setView(input);

        // Кнопки диалога
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    saveUserName(newName);
                    tvUsername.setText("👤 " + newName);
                    Toast.makeText(DashboardActivity.this, "Имя успешно изменено!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Показать диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getCurrentUserName() {
        String currentUser = sharedPreferences.getString(Constants.KEY_CURRENT_USER, "Пользователь");
        // Убираем эмодзи если есть в начале
        if (currentUser.startsWith("👤 ")) {
            return currentUser.substring(2);
        }
        return currentUser;
    }

    private void saveUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_CURRENT_USER, name);
        editor.apply();
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, false);
        editor.remove(Constants.KEY_CURRENT_USER);
        editor.apply();

        Toast.makeText(this, "Выход выполнен", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}