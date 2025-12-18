package com.example.project_m;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class TestMicrophoneActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_microphone);

        tvStatus = findViewById(R.id.tvStatus);
        btnTest = findViewById(R.id.btnTest);

        btnTest.setOnClickListener(v -> testMicrophone());
    }

    private void testMicrophone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 123);
            return;
        }

        try {
            AudioRecord recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    1024
            );

            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                tvStatus.setText("❌ Микрофон недоступен");
                return;
            }

            recorder.startRecording();

            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                tvStatus.setText("✅ Микрофон работает нормально");

                // Тестируем запись
                short[] buffer = new short[1024];
                int read = recorder.read(buffer, 0, 1024);

                if (read > 0) {
                    tvStatus.setText("✅ Микрофон работает! Запись: " + read + " samples");
                }
            } else {
                tvStatus.setText("❌ Не удалось начать запись");
            }

            recorder.stop();
            recorder.release();

        } catch (Exception e) {
            tvStatus.setText("❌ Ошибка: " + e.getMessage());
        }
    }
}