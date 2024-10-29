package ua.teri.geoquiz;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    private ImageButton mTrueButton;
    private ImageButton mFalseButton;
    private TextView mTextQuiz;
    private TextView mScoreText;

    private static final int QUESTIONS_PER_GAME = 2; // Number of questions per game
    private int mScore = 0;
    private Set<Integer> mAnsweredQuestions = new HashSet<>();
    private List<Integer> mCurrentGameQuestions = new ArrayList<>();

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_1, true),
            new Question(R.string.question_2, false)
    };

    private int mCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mTextQuiz = findViewById(R.id.TextQuiz);
        mTrueButton = findViewById(R.id.buttonTrue);
        mFalseButton = findViewById(R.id.buttonFalse);
        mScoreText = findViewById(R.id.score_text);

        initializeGame();
        updateQuestion();

        mTrueButton.setOnClickListener(view -> checkAnswer(true));
        mFalseButton.setOnClickListener(view -> checkAnswer(false));
    }

    private void initializeGame() {
        mScore = 0;
        mAnsweredQuestions.clear();
        mCurrentGameQuestions.clear();
        
        // Create list of all question indices
        List<Integer> allQuestions = new ArrayList<>();
        for (int i = 0; i < mQuestionBank.length; i++) {
            allQuestions.add(i);
        }
        
        // Randomly select questions for this game
        Random random = new Random();
        int questionsToSelect = Math.min(QUESTIONS_PER_GAME, mQuestionBank.length);
        while (mCurrentGameQuestions.size() < questionsToSelect) {
            int index = random.nextInt(allQuestions.size());
            mCurrentGameQuestions.add(allQuestions.get(index));
            allQuestions.remove(index);
        }
        
        mCurrentIndex = 0;
        updateScoreDisplay();
    }

    private void updateScoreDisplay() {
        mScoreText.setText(getString(R.string.score_format, mScore, QUESTIONS_PER_GAME));
    }

    private void updateQuestion() {
        if (mCurrentIndex >= mCurrentGameQuestions.size()) {
            showGameOver();
            return;
        }

        int questionIndex = mCurrentGameQuestions.get(mCurrentIndex);
        int question = mQuestionBank[questionIndex].getTextResId();
        mTextQuiz.setText(question);
        mTextQuiz.setTextColor(Color.BLACK); // Reset text color
        
        // Enable/disable buttons based on whether question was answered
        boolean questionAnswered = mAnsweredQuestions.contains(questionIndex);
        mTrueButton.setEnabled(!questionAnswered);
        mFalseButton.setEnabled(!questionAnswered);
    }

    private void checkAnswer(boolean userPressedTrue) {
        int questionIndex = mCurrentGameQuestions.get(mCurrentIndex);
        if (mAnsweredQuestions.contains(questionIndex)) {
            Toast.makeText(this, R.string.already_answered, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean answerIsTrue = mQuestionBank[questionIndex].isAnswerTrue();
        if (answerIsTrue == userPressedTrue) {
            mScore++;
            mTextQuiz.setTextColor(Color.GREEN);
        } else {
            mTextQuiz.setTextColor(Color.RED);
        }

        mAnsweredQuestions.add(questionIndex);
        updateScoreDisplay();

        int messageResId = answerIsTrue == userPressedTrue ? R.string.toast_true : R.string.toast_false;
        Toast toast = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        // Move to next question after a short delay
        new Handler().postDelayed(() -> {
            mCurrentIndex++;
            updateQuestion();
        }, 1500);
    }

    private void showGameOver() {
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        String gameOverMessage = getString(R.string.game_over_format, mScore, QUESTIONS_PER_GAME);
        mTextQuiz.setText(gameOverMessage);
        mTextQuiz.setTextColor(Color.BLACK);
        
        // Show replay option
        Toast toast = Toast.makeText(this, R.string.tap_to_play_again, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
        
        mTextQuiz.setOnClickListener(v -> {
            initializeGame();
            updateQuestion();
            mTextQuiz.setOnClickListener(null);
        });
    }
}
