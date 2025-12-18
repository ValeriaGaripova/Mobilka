package com.example.project_m;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecorderHelper {
    private MediaRecorder mediaRecorder;
    private String currentFilePath;
    private Context context;

    public AudioRecorderHelper(Context context) {
        this.context = context;
    }

    public String startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            // Используем внутреннее хранилище приложения
            currentFilePath = getInternalStorageFilePath();
            mediaRecorder.setOutputFile(currentFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            return currentFilePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getInternalStorageFilePath() {
        // Используем внутреннее хранилище приложения
        File storageDir = context.getFilesDir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "AUDIO_" + timeStamp + ".m4a";

        return new File(storageDir, fileName).getAbsolutePath();
    }

    public void release() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}