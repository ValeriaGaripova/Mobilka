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

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        EditText editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonBack = findViewById(R.id.buttonBack);

        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            String existingEmail = sharedPreferences.getString(Constants.KEY_EMAIL, "");
            if (!existingEmail.isEmpty() && existingEmail.equals(email)) {
                Toast.makeText(this, "Этот email уже зарегистрирован", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.KEY_EMAIL, email);
            editor.putString(Constants.KEY_PASSWORD, password);
            editor.putString(Constants.KEY_CURRENT_USER, email);
            editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
            editor.apply();

            Toast.makeText(this, "✅ Регистрация успешна!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        buttonBack.setOnClickListener(v -> finish());
    }
}