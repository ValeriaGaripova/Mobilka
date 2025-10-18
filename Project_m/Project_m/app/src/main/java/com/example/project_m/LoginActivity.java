package com.example.project_m;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkCredentials(email, password)) {
                saveUserSession(email);
                Toast.makeText(this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();

                // ПЕРЕХОДИМ НА ГЛАВНЫЙ ЭКРАН
                Intent intent = new Intent(LoginActivity.this, AudioRecorderActivity.class);
                startActivity(intent);
                // НЕ ВЫЗЫВАЕМ finish() - чтобы можно было вернуться назад
            } else {
                Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
            }
        });

        buttonRegister.setOnClickListener(v -> {
            // ПЕРЕХОДИМ НА РЕГИСТРАЦИЮ
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkCredentials(String email, String password) {
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");

        // Для теста - если нет зарегистрированных пользователей, разрешаем любой вход
        if (savedEmail.isEmpty()) {
            return true;
        }

        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    private void saveUserSession(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("current_user", email);
        editor.putBoolean("is_logged_in", true); // ДОБАВИЛИ ФЛАГ ВХОДА
        editor.apply();
    }
}