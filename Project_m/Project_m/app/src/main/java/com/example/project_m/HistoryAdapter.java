package com.example.project_m;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<DatabaseHelper.Record> records;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private String currentPlayingPath = null;
    private int currentPlayingPosition = -1; // Добавляем отслеживание позиции

    public HistoryAdapter(List<DatabaseHelper.Record> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.Record record = records.get(position);
        holder.bind(record, position);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public void updateData(List<DatabaseHelper.Record> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    private void stopCurrentPlayback() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            currentPlayingPath = null;
            int oldPosition = currentPlayingPosition;
            currentPlayingPosition = -1;

            // Уведомляем об изменении старой позиции, чтобы обновить кнопку
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
        }
    }

    private void playAudio(String filePath, ViewHolder holder, int position) {
        stopCurrentPlayback();

        File audioFile = new File(filePath);
        if (!audioFile.exists() || audioFile.length() == 0) {
            Toast.makeText(holder.itemView.getContext(),
                    "Аудиофайл не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            isPlaying = true;
            currentPlayingPath = filePath;
            currentPlayingPosition = position;

            // Обновляем только текущую позицию
            notifyItemChanged(position);

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                currentPlayingPath = null;
                int oldPos = currentPlayingPosition;
                currentPlayingPosition = -1;

                // Обновляем старую позицию после завершения воспроизведения
                if (oldPos != -1) {
                    notifyItemChanged(oldPos);
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(holder.itemView.getContext(),
                        "Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
                isPlaying = false;
                currentPlayingPath = null;
                int oldPos = currentPlayingPosition;
                currentPlayingPosition = -1;

                if (oldPos != -1) {
                    notifyItemChanged(oldPos);
                }
                return true;
            });

            Toast.makeText(holder.itemView.getContext(),
                    "Воспроизведение...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(holder.itemView.getContext(),
                    "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNote;
        public TextView tvAccuracy;
        public TextView tvFrequency;
        public TextView tvDuration;
        public TextView tvTime;
        public Button btnPlay;

        public ViewHolder(View view) {
            super(view);
            tvNote = view.findViewById(R.id.tvNote);
            tvAccuracy = view.findViewById(R.id.tvAccuracy);
            tvFrequency = view.findViewById(R.id.tvFrequency);
            tvDuration = view.findViewById(R.id.tvDuration);
            tvTime = view.findViewById(R.id.tvTime);
            btnPlay = view.findViewById(R.id.btnPlay);
        }

        public void bind(DatabaseHelper.Record record, int position) {
            tvNote.setText("Нота: " + record.getNote());
            tvAccuracy.setText("Точность: " + record.getAccuracy() + "%");
            tvFrequency.setText("Частота: " + record.getFrequency() + " Гц");
            tvDuration.setText("Длительность: " + record.getDuration());
            tvTime.setText(record.getTimestamp());

            // Проверяем, есть ли аудиофайл
            boolean hasAudio = record.hasAudioFile();
            btnPlay.setEnabled(hasAudio);

            if (hasAudio) {
                // Проверяем, играет ли сейчас эта запись
                boolean isThisPlaying = currentPlayingPosition == position && isPlaying;
                btnPlay.setText(isThisPlaying ? "⏹️ Стоп" : "▶️ Прослушать");

                btnPlay.setOnClickListener(v -> {
                    if (isThisPlaying) {
                        stopCurrentPlayback();
                        // Кнопка обновится через notifyItemChanged в stopCurrentPlayback
                    } else {
                        playAudio(record.getAudioPath(), this, position);
                    }
                });
            } else {
                btnPlay.setText("🎵 Нет аудио");
                btnPlay.setOnClickListener(v -> {
                    Toast.makeText(itemView.getContext(),
                            "Аудиозапись не найдена", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        stopCurrentPlayback();
    }
}