package com.example.project_m;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class AudioRecorderActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentAudioPath;
    private boolean isRecording = false;
    private int recordingTime = 0;
    private Handler timerHandler = new Handler();

    private Button buttonRecord, buttonStop, buttonPlay;
    private TextView tvTimer, tvRecordStatus, tvNote, tvAccuracy, tvFrequency;

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        initViews();

        // Автоматически запрашиваем разрешения
        if (!hasPermissions()) {
            requestPermissions();
        } else {
            enableRecording();
        }
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvRecordStatus = findViewById(R.id.tvRecordStatus);
        tvNote = findViewById(R.id.tvNote);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFrequency = findViewById(R.id.tvFrequency);

        buttonRecord = findViewById(R.id.buttonRecord);
        buttonStop = findViewById(R.id.buttonStop);
        buttonPlay = findViewById(R.id.buttonPlay);

        Button buttonLogout = findViewById(R.id.buttonLogout);
        Button historyButton = findViewById(R.id.history_button);

        buttonRecord.setOnClickListener(v -> startRecording());
        buttonStop.setOnClickListener(v -> stopRecording());
        buttonPlay.setOnClickListener(v -> playRecording());

        buttonLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        historyButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

        updateButtons(false, false, false);
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableRecording();
                Toast.makeText(this, "Разрешения получены!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Нужны разрешения для записи", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void enableRecording() {
        updateButtons(true, false, false);
    }

    private void updateButtons(boolean record, boolean stop, boolean play) {
        buttonRecord.setEnabled(record);
        buttonStop.setEnabled(stop);
        buttonPlay.setEnabled(play);
    }

    private void startRecording() {
        try {
            // Сброс
            resetUI();

            // Создаем файл
            File audioFile = new File(getExternalFilesDir(null), "recording_" + System.currentTimeMillis() + ".3gp");
            currentAudioPath = audioFile.getAbsolutePath();

            // Настраиваем запись
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(currentAudioPath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            updateButtons(false, true, false);
            tvRecordStatus.setText("Запись идет...");

            // Запускаем таймер
            startTimer();

            Toast.makeText(this, "Запись начата!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            updateButtons(true, false, false);
        }
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    recordingTime++;
                    updateTimer();
                    timerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void updateTimer() {
        int minutes = recordingTime / 60;
        int seconds = recordingTime % 60;
        String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(time);
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        isRecording = false;
        timerHandler.removeCallbacksAndMessages(null);
        updateButtons(true, false, true);
        tvRecordStatus.setText("Запись завершена");

        // Сохраняем результат
        saveResult();

        Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show();
    }

    private void saveResult() {
        String note = "A4";
        String accuracy = "95%";
        String frequency = "440 Гц";

        tvNote.setText("🎵 Нота: " + note);
        tvAccuracy.setText("🎯 Точность: " + accuracy);
        tvFrequency.setText("📊 Частота: " + frequency);

        // Сохраняем в базу
        new DatabaseHelper(this).addRecord(note, accuracy, frequency);
    }

    private void playRecording() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(currentAudioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            tvRecordStatus.setText("Воспроизведение...");
            buttonPlay.setEnabled(false);

            mediaPlayer.setOnCompletionListener(mp -> {
                tvRecordStatus.setText("Воспроизведение завершено");
                buttonPlay.setEnabled(true);
            });

        } catch (IOException e) {
            Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
            buttonPlay.setEnabled(true);
        }
    }

    private void resetUI() {
        recordingTime = 0;
        updateTimer();
        tvNote.setText("🎵 Нота: --");
        tvAccuracy.setText("🎯 Точность: --");
        tvFrequency.setText("📊 Частота: -- Гц");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) mediaRecorder.release();
        if (mediaPlayer != null) mediaPlayer.release();
        timerHandler.removeCallbacksAndMessages(null);
    }
}