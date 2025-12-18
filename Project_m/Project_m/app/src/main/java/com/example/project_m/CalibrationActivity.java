package com.example.project_m;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class CalibrationActivity extends AppCompatActivity {

    private SeekBar pitchSeekBar;
    private TextView currentNoteText;
    private TextView frequencyText;
    private TextView centsText;
    private Button playButton;
    private Button backButton;
    private Spinner noteSpinner;

    private double currentFrequency = 440.0;
    private AudioTrack audioTrack;
    private boolean isPlaying = false;

    private Map<String, Double> noteFrequencies = new HashMap<String, Double>() {{
        put("C4", 261.63);
        put("C#4", 277.18);
        put("D4", 293.66);
        put("D#4", 311.13);
        put("E4", 329.63);
        put("F4", 349.23);
        put("F#4", 369.99);
        put("G4", 392.00);
        put("G#4", 415.30);
        put("A4", 440.00);
        put("A#4", 466.16);
        put("B4", 493.88);
        put("C5", 523.25);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        initializeViews();
        setupNoteSpinner();
        setupSeekBar();
        setupButtons();
        updateDisplay();
    }

    private void initializeViews() {
        pitchSeekBar = findViewById(R.id.pitchSeekBar);
        currentNoteText = findViewById(R.id.currentNoteText);
        frequencyText = findViewById(R.id.frequencyText);
        centsText = findViewById(R.id.centsText);
        playButton = findViewById(R.id.playButton);
        backButton = findViewById(R.id.backButton);
        noteSpinner = findViewById(R.id.noteSpinner);
    }

    private void setupNoteSpinner() {
        String[] notes = noteFrequencies.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, notes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noteSpinner.setAdapter(adapter);
        noteSpinner.setSelection(9); // A4 по умолчанию

        // Обработчик изменения выбранной ноты
        noteSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedNote = (String) parent.getItemAtPosition(position);
                double referenceFreq = noteFrequencies.get(selectedNote);
                currentFrequency = referenceFreq;
                pitchSeekBar.setProgress(500); // Сброс к центру
                updateDisplay();
                if (isPlaying) {
                    stopSound();
                    playSound();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupSeekBar() {
        pitchSeekBar.setMax(1000); // -500 до +500 центов
        pitchSeekBar.setProgress(500); // Центр = 0 центов

        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double cents = (progress - 500);
                double referenceFreq = getReferenceFrequency();
                currentFrequency = referenceFreq * Math.pow(2, cents / 1200.0);
                updateDisplay();

                // Если звук играет, обновляем его
                if (isPlaying) {
                    stopSound();
                    playSound();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupButtons() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    stopSound();
                } else {
                    playSound();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSound();
                finish();
            }
        });
    }

    private double getReferenceFrequency() {
        String selectedNote = (String) noteSpinner.getSelectedItem();
        return noteFrequencies.get(selectedNote);
    }

    private void updateDisplay() {
        double referenceFreq = getReferenceFrequency();
        double cents = 1200 * Math.log(currentFrequency / referenceFreq) / Math.log(2);
        String noteName = getClosestNoteName(currentFrequency);

        currentNoteText.setText("Текущая нота: " + noteName);
        frequencyText.setText(String.format("Частота: %.1f Hz", currentFrequency));

        // Цвет текста в зависимости от отклонения
        int textColor;
        if (Math.abs(cents) < 10) {
            textColor = android.graphics.Color.parseColor("#2E7D32"); // Зеленый - точно
        } else if (Math.abs(cents) < 50) {
            textColor = android.graphics.Color.parseColor("#FF9800"); // Оранжевый - близко
        } else {
            textColor = android.graphics.Color.parseColor("#F44336"); // Красный - далеко
        }
        centsText.setTextColor(textColor);
        centsText.setText(String.format("Отклонение: %.1f центов", cents));

        String selectedNote = (String) noteSpinner.getSelectedItem();
        playButton.setText(isPlaying ? "Остановить звук" : "Проиграть " + selectedNote);
    }

    private String getClosestNoteName(double frequency) {
        String closestNote = "A4";
        double minDiff = Double.MAX_VALUE;

        for (Map.Entry<String, Double> entry : noteFrequencies.entrySet()) {
            double diff = Math.abs(frequency - entry.getValue());
            if (diff < minDiff) {
                minDiff = diff;
                closestNote = entry.getKey();
            }
        }

        return closestNote;
    }

    private void playSound() {
        stopSound();

        int sampleRate = 44100;
        int duration = 2000; // 2 секунды для более быстрого отклика
        int numSamples = duration * sampleRate / 1000;
        short[] samples = new short[numSamples];

        // Генерируем синусоидальную волну с плавным началом и концом
        for (int i = 0; i < numSamples; i++) {
            // Плавная атака и затухание
            double envelope = 1.0;
            if (i < 1000) envelope = i / 1000.0; // Атака
            if (i > numSamples - 1000) envelope = (numSamples - i) / 1000.0; // Затухание

            double sample = Math.sin(2 * Math.PI * i * currentFrequency / sampleRate);
            samples[i] = (short) (sample * Short.MAX_VALUE * envelope);
        }

        try {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    AudioTrack.MODE_STATIC
            );

            audioTrack.write(samples, 0, numSamples);
            audioTrack.setLoopPoints(0, numSamples, -1);
            audioTrack.play();
            isPlaying = true;
            updateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSound() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
        isPlaying = false;
        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
    }
}