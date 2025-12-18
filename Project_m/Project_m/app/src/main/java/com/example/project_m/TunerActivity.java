package com.example.project_m;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TunerActivity extends AppCompatActivity {
    private static final String TAG = "TunerActivity";

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private final Handler handler = new Handler();
    private long startTime = 0;
    private String currentRecordingPath;
    private DatabaseHelper databaseHelper;
    private String currentUserId;

    private AudioAnalyzer audioAnalyzer;

    private String currentNote = "--";
    private String currentAccuracy = "--";
    private String currentFrequency = "--";
    private long recordingDuration = 0;

    private static final float PLAYBACK_VOLUME = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("current_user", "default_user");
        currentUserId = userEmail;

        databaseHelper = new DatabaseHelper(this);

        Log.d(TAG, "TunerActivity created for user: " + currentUserId);
        initializeViews();
    }

    private void initializeViews() {
        Button buttonRecord = findViewById(R.id.buttonRecord);
        Button buttonStop = findViewById(R.id.buttonStop);
        Button buttonPlay = findViewById(R.id.buttonPlay);
        Button buttonBack = findViewById(R.id.buttonBack);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        buttonRecord.setOnClickListener(v -> startRecording());
        buttonStop.setOnClickListener(v -> stopRecording());
        buttonPlay.setOnClickListener(v -> {
            if (isPlaying) {
                stopPlaying();
            } else {
                playRecording();
            }
        });

        // ТОЛЬКО 3 КНОПКИ НАВИГАЦИИ ТЕПЕРЬ
        Button navProgress = findViewById(R.id.nav_progress);
        Button navExercises = findViewById(R.id.nav_exercises);
        Button navHistory = findViewById(R.id.nav_history);

        // УБРАЛ nav_achievements - теперь нет кнопки достижений

        navProgress.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProgressActivity.class);
            startActivity(intent);
        });

        navExercises.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExercisesActivity.class);
            startActivity(intent);
        });

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        buttonBack.setOnClickListener(v -> finish());

        buttonLogout.setOnClickListener(v -> {
            getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_in", false)
                    .apply();

            Intent intent = new Intent(TunerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        updateUIState();
        updateNoteInfo("--", "--", "--");
    }

    private boolean isMicrophoneAvailable() {
        if (!checkAudioPermission()) {
            Log.d(TAG, "No permission for microphone check");
            return false;
        }

        AudioRecord recorder = null;
        try {
            int bufferSize = AudioRecord.getMinBufferSize(44100,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size");
                return false;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized");
                return false;
            }

            recorder.startRecording();
            boolean isRecording = recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;

            if (isRecording) {
                recorder.stop();
            }

            return isRecording;

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in microphone check: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Microphone check failed: " + e.getMessage());
            return false;
        } finally {
            if (recorder != null) {
                try {
                    recorder.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing AudioRecord: " + e.getMessage());
                }
            }
        }
    }

    private void startRecording() {
        if (!checkAudioPermission()) {
            Log.d(TAG, "No permission, requesting...");
            requestAudioPermission();
            return;
        }

        try {
            if (!isMicrophoneAvailable()) {
                Toast.makeText(this, "❌ Микрофон недоступен или занят другим приложением", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in microphone availability check: " + e.getMessage());
            Toast.makeText(this, "❌ Нет разрешения на использование микрофона", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            stopRecording();
            stopPlaying();

            setupMediaRecorder();
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            startTime = System.currentTimeMillis();
            startTimer();
            startRealPitchDetection();

            updateUIState();
            Toast.makeText(this, "🎤 Запись начата - говорите/пойте в микрофон!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Recording start failed: " + e.getMessage());
            Toast.makeText(this, "❌ Ошибка записи: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in recording: " + e.getMessage());
            Toast.makeText(this, "❌ Нет разрешения на запись аудио", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            Toast.makeText(this, "❌ Неожиданная ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupMediaRecorder() throws IOException, SecurityException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("No RECORD_AUDIO permission");
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioEncodingBitRate(128000);

        File recordingsDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "vocal_tuner");
        if (!recordingsDir.exists()) {
            boolean created = recordingsDir.mkdirs();
            Log.d(TAG, "Recordings directory created: " + created);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        currentRecordingPath = new File(recordingsDir, "recording_" + timestamp + ".m4a").getAbsolutePath();
        mediaRecorder.setOutputFile(currentRecordingPath);

        Log.d(TAG, "Recording to: " + currentRecordingPath);
    }

    private void startRealPitchDetection() {
        if (audioAnalyzer != null) {
            audioAnalyzer.stopRecording();
        }

        audioAnalyzer = new AudioAnalyzer();
        audioAnalyzer.startRecording(this, (note, frequency, amplitude) -> {
            Log.d(TAG, "AudioAnalyzer detected - Note: " + note + ", Freq: " + frequency + ", Amp: " + amplitude);

            int accuracy = calculateAccuracyBasedOnAmplitude(amplitude);
            runOnUiThread(() -> {
                currentNote = note;
                currentAccuracy = String.valueOf(accuracy);
                currentFrequency = String.format(Locale.getDefault(), "%.1f", frequency);

                updateNoteInfo(note, currentAccuracy, currentFrequency);
            });
        });
    }

    private int calculateAccuracyBasedOnAmplitude(double amplitude) {
        if (amplitude > -10) return 95;
        if (amplitude > -20) return 85;
        if (amplitude > -30) return 75;
        if (amplitude > -35) return 65;
        return 50;
    }

    private void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                Log.d(TAG, "Recording stopped successfully. File: " + currentRecordingPath);
            } catch (Exception e) {
                Log.e(TAG, "MediaRecorder stop error: " + e.getMessage());
            }

            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            recordingDuration = System.currentTimeMillis() - startTime;

            if (audioAnalyzer != null) {
                audioAnalyzer.stopRecording();
            }

            handler.removeCallbacksAndMessages(null);
            updateUIState();

            if (recordingDuration > 1000) {
                saveRecordingToDatabase();
                Toast.makeText(this, "⏹️ Запись сохранена (" + (recordingDuration/1000) + " сек)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Запись слишком короткая", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playRecording() {
        if (currentRecordingPath == null) {
            Log.e(TAG, "playRecording: currentRecordingPath is null");
            Toast.makeText(this, "❌ Сначала сделайте запись", Toast.LENGTH_SHORT).show();
            return;
        }

        File recordingFile = new File(currentRecordingPath);
        Log.d(TAG, "playRecording: File exists: " + recordingFile.exists() + ", size: " + recordingFile.length() + ", path: " + currentRecordingPath);

        if (!recordingFile.exists() || recordingFile.length() == 0) {
            Log.e(TAG, "playRecording: File not found or empty");
            Toast.makeText(this, "❌ Файл записи не найден или пустой", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            stopPlaying();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(currentRecordingPath);
            mediaPlayer.prepare();

            mediaPlayer.setVolume(PLAYBACK_VOLUME, PLAYBACK_VOLUME);
            mediaPlayer.start();

            isPlaying = true;
            updateUIState();

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                updateUIState();
                Toast.makeText(TunerActivity.this, "▶️ Воспроизведение завершено", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                Toast.makeText(TunerActivity.this, "❌ Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
                isPlaying = false;
                updateUIState();
                return true;
            });

            Toast.makeText(this, "▶️ Воспроизведение...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Playing recording successfully");

        } catch (IOException e) {
            Log.e(TAG, "Playback failed: " + e.getMessage());
            Toast.makeText(this, "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected playback error: " + e.getMessage());
            Toast.makeText(this, "❌ Неожиданная ошибка", Toast.LENGTH_LONG).show();
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            updateUIState();
        }
    }

    private void updateUIState() {
        runOnUiThread(() -> {
            Button buttonRecord = findViewById(R.id.buttonRecord);
            Button buttonStop = findViewById(R.id.buttonStop);
            Button buttonPlay = findViewById(R.id.buttonPlay);
            TextView tvRecordStatus = findViewById(R.id.tvRecordStatus);

            buttonRecord.setEnabled(!isRecording && !isPlaying);
            buttonStop.setEnabled(isRecording || isPlaying);
            buttonPlay.setEnabled(!isRecording && currentRecordingPath != null);

            if (isRecording) {
                tvRecordStatus.setText("🎤 Идет запись... Пойте в микрофон!");
                buttonPlay.setText("▶️ Прослушать");
            } else if (isPlaying) {
                tvRecordStatus.setText("▶️ Идет воспроизведение...");
                buttonPlay.setText("⏹️ Стоп");
            } else {
                if (currentRecordingPath != null) {
                    tvRecordStatus.setText("Запись готова к прослушиванию");
                    buttonPlay.setText("▶️ Прослушать");
                } else {
                    tvRecordStatus.setText("Нажмите запись и пойте в микрофон");
                    buttonPlay.setText("▶️ Прослушать");
                }
            }
        });
    }

    private void startTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long millis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;

                    TextView tvTimer = findViewById(R.id.tvTimer);
                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void updateNoteInfo(String note, String accuracy, String frequency) {
        runOnUiThread(() -> {
            currentNote = note;
            currentAccuracy = accuracy;
            currentFrequency = frequency;

            TextView tvNote = findViewById(R.id.tvNote);
            TextView tvAccuracy = findViewById(R.id.tvAccuracy);
            TextView tvFrequency = findViewById(R.id.tvFrequency);

            tvNote.setText("🎵 Нота: " + note);
            tvAccuracy.setText("🎯 Точность: " + accuracy + "%");
            tvFrequency.setText("📊 Частота: " + frequency + " Гц");
        });
    }

    private void saveRecordingToDatabase() {
        try {
            String duration = String.format(Locale.getDefault(), "%d сек", recordingDuration / 1000);
            String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());

            Log.d(TAG, "Saving record to database:");
            Log.d(TAG, "User: " + currentUserId);
            Log.d(TAG, "Note: " + currentNote);
            Log.d(TAG, "Accuracy: " + currentAccuracy);
            Log.d(TAG, "Frequency: " + currentFrequency);
            Log.d(TAG, "Duration: " + duration);
            Log.d(TAG, "Timestamp: " + timestamp);
            Log.d(TAG, "Audio path: " + currentRecordingPath);

            // Используем обновленный метод с аудиопутём
            boolean isInserted = databaseHelper.addRecord(
                    currentUserId,
                    currentNote,
                    currentAccuracy,
                    currentFrequency,
                    duration,
                    timestamp,
                    currentRecordingPath // Добавляем путь к аудиофайлу
            );

            if (isInserted) {
                Log.d(TAG, "Recording saved to database SUCCESS");
                Toast.makeText(this, "✅ Данные и аудиозапись сохранены", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Failed to save recording to database");
                Toast.makeText(this, "⚠️ Не удалось сохранить запись", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Database save error: " + e.getMessage());
            Toast.makeText(this, "❌ Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAudioPermission() {
        boolean hasRecordPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "Record permission: " + hasRecordPermission);
        return hasRecordPermission;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                123);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Разрешение получено", Toast.LENGTH_SHORT).show();
                startRecording();
            } else {
                Toast.makeText(this, "❌ Нужно разрешение на микрофон", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
        stopPlaying();
        if (audioAnalyzer != null) {
            audioAnalyzer.stopRecording();
        }
        handler.removeCallbacksAndMessages(null);
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}