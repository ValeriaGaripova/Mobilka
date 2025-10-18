package com.example.project_m;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonBack = findViewById(R.id.buttonBack);

        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password);
        });

        buttonBack.setOnClickListener(v -> {
            // ВОЗВРАЩАЕМСЯ НА АВТОРИЗАЦИЮ
            finish();
        });
    }

    private void registerUser(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("is_logged_in", true); // АВТОМАТИЧЕСКИ ВХОДИМ ПОСЛЕ РЕГИСТРАЦИИ
        editor.apply();

        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

        // ПЕРЕХОДИМ НА ГЛАВНЫЙ ЭКРАН
        Intent intent = new Intent(RegisterActivity.this, AudioRecorderActivity.class);
        startActivity(intent);
        finish(); // ЗАКРЫВАЕМ РЕГИСТРАЦИЮ
    }
}