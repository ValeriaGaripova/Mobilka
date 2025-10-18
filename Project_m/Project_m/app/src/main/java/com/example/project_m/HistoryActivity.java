package com.example.project_m;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private ListView historyListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);
        historyListView = findViewById(R.id.history_list_view);

        loadHistory();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        Cursor cursor = databaseHelper.getAllRecords();

        // Проверяем есть ли записи
        TextView emptyText = findViewById(R.id.empty_history_text);
        if (cursor.getCount() == 0) {
            emptyText.setText("История записей пуста");
        } else {
            emptyText.setVisibility(android.view.View.GONE);
        }

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.history_list_item,
                cursor,
                new String[] {
                        DatabaseHelper.COLUMN_NOTE,
                        DatabaseHelper.COLUMN_ACCURACY,
                        DatabaseHelper.COLUMN_FREQUENCY,
                        DatabaseHelper.COLUMN_TIMESTAMP
                },
                new int[] {
                        R.id.history_note,
                        R.id.history_accuracy,
                        R.id.history_frequency,
                        R.id.history_timestamp
                },
                0
        );

        historyListView.setAdapter(adapter);
    }
}