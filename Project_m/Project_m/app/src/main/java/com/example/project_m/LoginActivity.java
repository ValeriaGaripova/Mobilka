package com.example.project_m;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        initializeViews();
    }

    private void initializeViews() {
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                return;
            }

            String savedEmail = sharedPreferences.getString(Constants.KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(Constants.KEY_PASSWORD, "");

            if (savedEmail.isEmpty()) {
                Toast.makeText(this, "❌ Аккаунт не найден. Сначала зарегистрируйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equals(savedEmail) && password.equals(savedPassword)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.KEY_CURRENT_USER, email);
                editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
                editor.apply();

                Toast.makeText(this, "✅ Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "❌ Неверный email или пароль", Toast.LENGTH_SHORT).show();
            }
        });

        buttonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ПЕРЕХОД НА ЭКРАН ВОССТАНОВЛЕНИЯ ПАРОЛЯ
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}