package com.example.project_m;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.util.Arrays;

public class AudioAnalyzer {
    private static final String TAG = "AudioAnalyzer";

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread analysisThread;

    // Частоты нот (в Гц)
    private final double[] NOTE_FREQUENCIES = {
            16.35, 17.32, 18.35, 19.45, 20.60, 21.83, 23.12, 24.50, 25.96, 27.50, 29.14, 30.87, // C0-B0
            32.70, 34.65, 36.71, 38.89, 41.20, 43.65, 46.25, 49.00, 51.91, 55.00, 58.27, 61.74, // C1-B1
            65.41, 69.30, 73.42, 77.78, 82.41, 87.31, 92.50, 98.00, 103.83, 110.00, 116.54, 123.47, // C2-B2
            130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65, 220.00, 233.08, 246.94, // C3-B3
            261.63, 277.18, 293.66, 311.13, 329.63, 349.23, 369.99, 392.00, 415.30, 440.00, 466.16, 493.88, // C4-B4 (средняя октава)
            523.25, 554.37, 587.33, 622.25, 659.25, 698.46, 739.99, 783.99, 830.61, 880.00, 932.33, 987.77, // C5-B5
            1046.50, 1108.73, 1174.66, 1244.51, 1318.51, 1396.91, 1479.98, 1567.98, 1661.22, 1760.00, 1864.66, 1975.53  // C6-B6
    };

    private final String[] NOTE_NAMES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    public interface AnalysisCallback {
        void onFrequencyDetected(double frequency, String noteName, double accuracy);
        void onError(String error);
    }

    public void startAnalysis(AnalysisCallback callback) {
        if (isRecording) {
            return;
        }

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                callback.onError("Не удалось инициализировать AudioRecord");
                return;
            }

            audioRecord.startRecording();
            isRecording = true;

            analysisThread = new Thread(() -> {
                analyzeAudio(callback);
            });
            analysisThread.start();

            Log.d(TAG, "Audio analysis started");

        } catch (SecurityException e) {
            callback.onError("Нет разрешения на запись аудио");
        } catch (Exception e) {
            callback.onError("Ошибка запуска анализа: " + e.getMessage());
        }
    }

    public void stopAnalysis() {
        isRecording = false;

        if (analysisThread != null) {
            try {
                analysisThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping analysis thread", e);
            }
            analysisThread = null;
        }

        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing audio record", e);
            }
            audioRecord = null;
        }

        Log.d(TAG, "Audio analysis stopped");
    }

    private void analyzeAudio(AnalysisCallback callback) {
        short[] audioBuffer = new short[BUFFER_SIZE];

        while (isRecording) {
            int bytesRead = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);

            if (bytesRead > 0) {
                double frequency = calculateFrequency(audioBuffer, bytesRead);

                if (frequency > 0) {
                    NoteResult noteResult = findClosestNote(frequency);
                    callback.onFrequencyDetected(frequency, noteResult.noteName, noteResult.accuracy);
                }
            }

            try {
                Thread.sleep(100); // Анализируем 10 раз в секунду
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private double calculateFrequency(short[] audioData, int length) {
        // Простой алгоритм подсчета нулей для определения частоты
        int zeroCrossings = 0;

        for (int i = 1; i < length; i++) {
            if ((audioData[i] >= 0 && audioData[i-1] < 0) ||
                    (audioData[i] < 0 && audioData[i-1] >= 0)) {
                zeroCrossings++;
            }
        }

        double duration = (double) length / SAMPLE_RATE;
        double frequency = zeroCrossings / (2 * duration);

        // Фильтруем слишком низкие и высокие частоты
        if (frequency < 50 || frequency > 2000) {
            return 0;
        }

        return frequency;
    }

    private NoteResult findClosestNote(double frequency) {
        double minDifference = Double.MAX_VALUE;
        int closestNoteIndex = -1;

        for (int i = 0; i < NOTE_FREQUENCIES.length; i++) {
            double difference = Math.abs(NOTE_FREQUENCIES[i] - frequency);
            if (difference < minDifference) {
                minDifference = difference;
                closestNoteIndex = i;
            }
        }

        if (closestNoteIndex != -1) {
            String noteName = NOTE_NAMES[closestNoteIndex % 12];
            int octave = closestNoteIndex / 12;
            double accuracy = Math.max(0, 100 - (minDifference / NOTE_FREQUENCIES[closestNoteIndex] * 100));

            return new NoteResult(noteName + octave, accuracy);
        }

        return new NoteResult("?", 0);
    }

    private static class NoteResult {
        String noteName;
        double accuracy;

        NoteResult(String noteName, double accuracy) {
            this.noteName = noteName;
            this.accuracy = accuracy;
        }
    }
}