package com.example.project_m;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextResetEmail;
    private Button buttonResetPassword;
    private TextView textViewBackToLogin;
    private ProgressBar progressBarReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Инициализация views
        editTextResetEmail = findViewById(R.id.editTextResetEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        textViewBackToLogin = findViewById(R.id.textViewBackToLogin);
        progressBarReset = findViewById(R.id.progressBarReset);

        // Обработчик кнопки сброса пароля
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        // Обработчик кнопки "Назад к входу"
        textViewBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Закрыть активити и вернуться к логину
            }
        });
    }

    private void resetPassword() {
        String email = editTextResetEmail.getText().toString().trim();

        // Валидация email
        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Показать прогресс бар и отключить кнопку
        progressBarReset.setVisibility(View.VISIBLE);
        buttonResetPassword.setEnabled(false);
        buttonResetPassword.setText("Отправляем...");

        // Симуляция отправки email (2 секунды)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Скрыть прогресс бар и включить кнопку
                progressBarReset.setVisibility(View.GONE);
                buttonResetPassword.setEnabled(true);
                buttonResetPassword.setText("Отправить ссылку");

                // Показать сообщение об успехе
                showSuccessMessage(email);
            }
        }, 2000);
    }

    private void showSuccessMessage(String email) {
        String message = "✅ Ссылка отправлена на " + email +
                "\n\nДля демо-версии используйте пароль: demo123";

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Автоматически закрыть через 3 секунды
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish(); // Вернуться к экрану логина
            }
        }, 3000);
    }
}