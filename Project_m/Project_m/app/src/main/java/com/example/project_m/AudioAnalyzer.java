package com.example.project_m;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.app.Activity;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;

public class AudioAnalyzer {
    private static final String TAG = "AudioAnalyzer";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;

    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;

    private static final double[] NOTE_FREQUENCIES = {
            130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65, 220.00, 233.08, 246.94,
            261.63, 277.18, 293.66, 311.13, 329.63, 349.23, 369.99, 392.00, 415.30, 440.00, 466.16, 493.88
    };

    private static final String[] NOTE_NAMES = {
            "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
            "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4"
    };

    public interface AnalysisCallback {
        void onNoteDetected(String note, double frequency, double amplitude);
    }

    public void startRecording(Activity activity, AnalysisCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (initializeAudioRecord()) {
                startAnalysis(callback);
            }
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION
            );
        }
    }

    private boolean initializeAudioRecord() {
        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord не инициализирован");
                return false;
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка инициализации: " + e.getMessage());
            return false;
        }
    }

    private void startAnalysis(AnalysisCallback callback) {
        if (audioRecord == null || audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord не готов к записи");
            return;
        }

        isRecording = true;
        recordingThread = new Thread(() -> {
            audioRecord.startRecording();
            short[] buffer = new short[BUFFER_SIZE];

            while (isRecording) {
                int bytesRead = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (bytesRead > 0) {
                    analyzeAudio(buffer, bytesRead, callback);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }

            stopAudioRecord();
        });

        recordingThread.start();
        Log.d(TAG, "Анализ звука запущен");
    }

    private void analyzeAudio(short[] buffer, int bytesRead, AnalysisCallback callback) {
        double amplitude = calculateAmplitude(buffer, bytesRead);

        if (amplitude > -40) {
            double detectedFrequency = calculateFrequencyAutocorrelation(buffer, bytesRead);
            String note = convertFrequencyToNote(detectedFrequency);

            if (callback != null && detectedFrequency > 100 && detectedFrequency < 1000) {
                callback.onNoteDetected(note, detectedFrequency, amplitude);
            }
        } else {
            if (callback != null) {
                callback.onNoteDetected("--", 0, amplitude);
            }
        }
    }

    private double calculateAmplitude(short[] buffer, int bytesRead) {
        if (buffer == null || bytesRead == 0) {
            return -100.0;
        }

        double sum = 0;
        for (int i = 0; i < bytesRead; i++) {
            sum += Math.abs(buffer[i]);
        }
        double average = sum / bytesRead;
        return 20 * Math.log10(average / 32768.0 + 1e-10);
    }

    private double calculateFrequencyAutocorrelation(short[] buffer, int bytesRead) {
        int maxLag = SAMPLE_RATE / 80;
        int minLag = SAMPLE_RATE / 1000;

        double maxCorrelation = -1;
        int bestLag = 0;

        for (int lag = minLag; lag < maxLag && lag < bytesRead/2; lag++) {
            double correlation = 0;
            int count = 0;

            for (int i = 0; i < bytesRead - lag; i++) {
                correlation += buffer[i] * buffer[i + lag];
                count++;
            }

            if (count > 0) {
                correlation /= count;
                if (correlation > maxCorrelation) {
                    maxCorrelation = correlation;
                    bestLag = lag;
                }
            }
        }

        if (bestLag > 0 && maxCorrelation > 1000000) {
            return (double) SAMPLE_RATE / bestLag;
        }

        return 0;
    }

    private String convertFrequencyToNote(double frequency) {
        if (frequency <= 100 || frequency >= 1000) {
            return "--";
        }

        double minDifference = Double.MAX_VALUE;
        int closestNoteIndex = -1;

        for (int i = 0; i < NOTE_FREQUENCIES.length; i++) {
            double difference = Math.abs(frequency - NOTE_FREQUENCIES[i]);
            double percentDifference = difference / NOTE_FREQUENCIES[i] * 100;

            if (difference < minDifference && percentDifference < 10) {
                minDifference = difference;
                closestNoteIndex = i;
            }
        }

        if (closestNoteIndex != -1) {
            return NOTE_NAMES[closestNoteIndex];
        }

        return String.format("%.0fГц", frequency);
    }

    private void stopAudioRecord() {
        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при остановке AudioRecord: " + e.getMessage());
            }
            audioRecord = null;
        }
    }

    public void stopRecording() {
        isRecording = false;

        if (recordingThread != null) {
            try {
                recordingThread.join(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "Ошибка остановки потока: " + e.getMessage());
            }
            recordingThread = null;
        }

        stopAudioRecord();
        Log.d(TAG, "Анализ звука остановлен");
    }
}