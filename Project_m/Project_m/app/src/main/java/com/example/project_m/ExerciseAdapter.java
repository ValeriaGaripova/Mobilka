package com.example.project_m;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
        void onStartExerciseClick(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exerciseList, OnExerciseClickListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.bind(exercise, listener);
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    // Метод для обновления данных
    public void updateExerciseList(List<Exercise> newList) {
        this.exerciseList = newList;
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvCategory, tvDuration, tvProgress;
        private ProgressBar progressBar;
        private Button btnSelect;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvExerciseTitle);
            tvCategory = itemView.findViewById(R.id.tvExerciseCategory);
            tvDuration = itemView.findViewById(R.id.tvExerciseDuration);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }

        public void bind(Exercise exercise, OnExerciseClickListener listener) {
            tvTitle.setText(exercise.getTitle());
            tvCategory.setText(exercise.getCategory());

            // Форматируем длительность
            int minutes = exercise.getDuration() / 60;
            int seconds = exercise.getDuration() % 60;
            tvDuration.setText(String.format("%d:%02d мин", minutes, seconds));

            // Устанавливаем прогресс
            tvProgress.setText(exercise.getProgress() + "%");
            progressBar.setProgress(exercise.getProgress());

            // Обработчики кликов
            itemView.setOnClickListener(v -> listener.onExerciseClick(exercise));
            btnSelect.setOnClickListener(v -> listener.onStartExerciseClick(exercise));
        }
    }
}