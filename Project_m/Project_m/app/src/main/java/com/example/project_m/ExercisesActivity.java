package com.example.project_m;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExercisesActivity extends AppCompatActivity implements ExerciseAdapter.OnExerciseClickListener {

    private RecyclerView exercisesRecyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;
    private SharedPreferences sharedPreferences;

    private CountDownTimer countDownTimer;
    private TextView tvTimer;
    private ProgressBar progressBar;
    private Button btnStartStop;
    private Exercise currentExercise;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        initializeViews();
        setupExercises();
        setupRecyclerView();
    }

    private void initializeViews() {
        Button btnBack = findViewById(R.id.btnBack);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);
        btnStartStop = findViewById(R.id.btnStartStop);
        Button btnChooseOther = findViewById(R.id.btnChooseOther);

        btnBack.setOnClickListener(v -> finish());
        btnStartStop.setOnClickListener(v -> toggleTimer());
        btnChooseOther.setOnClickListener(v -> hideExerciseDetails());

        progressBar.setProgress(0);
        progressBar.setMax(100);
    }

    private void setupExercises() {
        exerciseList = new ArrayList<>();

        exerciseList.add(new Exercise(
                "warmup_1",
                "🎵 Дыхательная гимнастика",
                "Подготовка голосовых связок",
                "Сделайте глубокий вдох через нос, медленно выдохните через рот. Повторите 5 раз.",
                "Разминка",
                300
        ));

        exerciseList.add(new Exercise(
                "warmup_2",
                "💨 Губные трели",
                "Разминка губ и диафрагмы",
                "Сомкните губы и создавайте вибрацию звуком 'brrr'. Начните с низких нот, поднимайтесь выше.",
                "Разминка",
                180
        ));

        exerciseList.add(new Exercise(
                "range_1",
                "🎼 Глиссандо вверх-вниз",
                "Расширение вокального диапазона",
                "На звуке 'ааа' плавно поднимитесь от самой низкой до самой высокой ноты и обратно.",
                "Диапазон",
                240
        ));

        exerciseList.add(new Exercise(
                "range_2",
                "⚡ Быстрые арпеджио",
                "Развитие гибкости голоса",
                "Быстро пропойте последовательность нот: до-ми-соль-до-соль-ми-до.",
                "Диапазон",
                180
        ));

        exerciseList.add(new Exercise(
                "accuracy_1",
                "🎯 Точные интервалы",
                "Тренировка музыкального слуха",
                "Пойте чистые интервалы: прима, терция, квинта, октава. Сконцентрируйтесь на чистоте звука.",
                "Точность",
                300
        ));

        exerciseList.add(new Exercise(
                "accuracy_2",
                "🎹 Пение по нотам",
                "Развитие интонации",
                "Попробуйте точно попадать в ноты C4, D4, E4, F4, G4. Слушайте себя внимательно.",
                "Точность",
                240
        ));

        loadProgress();
    }

    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(exerciseList, this);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(exerciseAdapter);
    }

    private void loadProgress() {
        String currentUserEmail = sharedPreferences.getString("current_user", "");

        for (Exercise exercise : exerciseList) {
            int progress = sharedPreferences.getInt("exercise_" + currentUserEmail + "_" + exercise.getId() + "_progress", 0);
            boolean completed = sharedPreferences.getBoolean("exercise_" + currentUserEmail + "_" + exercise.getId() + "_completed", false);
            exercise.setProgress(progress);
            exercise.setCompleted(completed);
        }
    }

    private void saveProgress(Exercise exercise) {
        String currentUserEmail = sharedPreferences.getString("current_user", "");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("exercise_" + currentUserEmail + "_" + exercise.getId() + "_progress", exercise.getProgress());
        editor.putBoolean("exercise_" + currentUserEmail + "_" + exercise.getId() + "_completed", exercise.isCompleted());

        // Сохраняем дату тренировки для подсчета серии
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        Set<String> datesSet = sharedPreferences.getStringSet("exercise_dates_" + currentUserEmail, new HashSet<>());
        Set<String> newDatesSet = new HashSet<>(datesSet);
        newDatesSet.add(today);

        editor.putStringSet("exercise_dates_" + currentUserEmail, newDatesSet);
        editor.apply();
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        showExerciseDetails(exercise);
    }

    @Override
    public void onStartExerciseClick(Exercise exercise) {
        showExerciseDetails(exercise);
    }

    private void showExerciseDetails(Exercise exercise) {
        currentExercise = exercise;

        TextView tvExerciseTitle = findViewById(R.id.tvExerciseTitle);
        TextView tvExerciseDesc = findViewById(R.id.tvExerciseDesc);
        TextView tvExerciseInstruction = findViewById(R.id.tvExerciseInstruction);

        tvExerciseTitle.setText(exercise.getTitle());
        tvExerciseDesc.setText(exercise.getDescription());
        tvExerciseInstruction.setText(exercise.getInstruction());

        resetTimer();

        findViewById(R.id.exerciseDetailPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.exerciseDetailPanel).requestFocus();
    }

    private void hideExerciseDetails() {
        findViewById(R.id.exerciseDetailPanel).setVisibility(View.GONE);

        if (isTimerRunning) {
            stopTimer();
        }

        currentExercise = null;
    }

    private void toggleTimer() {
        if (currentExercise == null) {
            Toast.makeText(this, "Сначала выберите упражнение", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isTimerRunning) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long startTimeInMillis;
        if (timeLeftInMillis > 0) {
            startTimeInMillis = timeLeftInMillis;
        } else {
            startTimeInMillis = currentExercise.getDuration() * 1000L;
        }

        countDownTimer = new CountDownTimer(startTimeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;

                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                int totalSeconds = currentExercise.getDuration();
                int elapsedSeconds = totalSeconds - (int)(millisUntilFinished / 1000);
                int progress = (int) ((float) elapsedSeconds / totalSeconds * 100);

                progressBar.setProgress(progress);
                currentExercise.setProgress(progress);
                saveProgress(currentExercise);
            }

            public void onFinish() {
                tvTimer.setText("00:00");
                progressBar.setProgress(100);
                isTimerRunning = false;
                timeLeftInMillis = 0;
                btnStartStop.setText("🎯 Начать");

                currentExercise.setCompleted(true);
                currentExercise.setProgress(100);
                saveProgress(currentExercise);

                updateExerciseInList(currentExercise);

                Toast.makeText(ExercisesActivity.this, "Упражнение завершено! 🎉", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        btnStartStop.setText("⏹️ Стоп");
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isTimerRunning = false;
        btnStartStop.setText("🎯 Начать");

        if (currentExercise != null && timeLeftInMillis > 0) {
            int totalSeconds = currentExercise.getDuration();
            int elapsedSeconds = totalSeconds - (int)(timeLeftInMillis / 1000);
            int progress = (int) ((float) elapsedSeconds / totalSeconds * 100);
            currentExercise.setProgress(progress);
            saveProgress(currentExercise);
        }

        Toast.makeText(this, "Упражнение приостановлено", Toast.LENGTH_SHORT).show();
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timeLeftInMillis = 0;
        isTimerRunning = false;
        btnStartStop.setText("🎯 Начать");

        if (currentExercise != null) {
            int minutes = currentExercise.getDuration() / 60;
            int seconds = currentExercise.getDuration() % 60;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            progressBar.setProgress(currentExercise.getProgress());
        } else {
            tvTimer.setText("05:00");
            progressBar.setProgress(0);
        }
    }

    private void updateExerciseInList(Exercise updatedExercise) {
        for (int i = 0; i < exerciseList.size(); i++) {
            Exercise exercise = exerciseList.get(i);
            if (exercise.getId().equals(updatedExercise.getId())) {
                exerciseList.set(i, updatedExercise);
                exerciseAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}