package com.example.project_m;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);
        Log.d(TAG, "HistoryActivity created");

        initializeViews();
        loadHistoryData();
    }

    private void initializeViews() {
        // Используем правильные ID из вашего макета
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        Button btnBack = findViewById(R.id.buttonBack);
        TextView tvEmpty = findViewById(R.id.tvEmptyHistory);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        HistoryAdapter adapter = new HistoryAdapter(new java.util.ArrayList<>());
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "Views initialized");
    }

    private void loadHistoryData() {
        String currentUser = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("current_user", "");

        Log.d(TAG, "Loading history for user: " + currentUser);

        if (!currentUser.isEmpty()) {
            List<DatabaseHelper.Record> records = databaseHelper.getUserRecords(currentUser);
            Log.d(TAG, "Loaded " + records.size() + " records from database");

            RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
            TextView tvEmpty = findViewById(R.id.tvEmptyHistory);
            HistoryAdapter adapter = (HistoryAdapter) recyclerView.getAdapter();

            if (adapter != null) {
                adapter.updateData(records);

                if (tvEmpty != null) {
                    tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
                }

                Log.d(TAG, "Adapter updated with " + records.size() + " records");
            }
        } else {
            Log.e(TAG, "No current user found in SharedPreferences");
            TextView tvEmpty = findViewById(R.id.tvEmptyHistory);
            if (tvEmpty != null) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Пользователь не найден");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}