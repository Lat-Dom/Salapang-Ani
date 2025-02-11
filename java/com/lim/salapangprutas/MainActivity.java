package com.lim.salapangprutas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class MainActivity extends AppCompatActivity {
    private TextView countdownText;
    private FrameLayout gameContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge drawing if desired.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> insets);
        setContentView(R.layout.activity_main); // Your XML layout

        countdownText = findViewById(R.id.countdownTextView);
        gameContainer = findViewById(R.id.gameContainer); // This FrameLayout will host the game view.

        startCountdown();
    }

    public void startCountdown() {
        new android.os.CountDownTimer(4000, 1000) { // 4-second countdown
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                runOnUiThread(() -> {
                    if (secondsRemaining == 3) {
                        countdownText.setText("3");
                        countdownText.setTextColor(Color.parseColor("#983e4e")); // Dark red
                    } else if (secondsRemaining == 2) {
                        countdownText.setText("2");
                        countdownText.setTextColor(Color.parseColor("#fbb844")); // Yellow-orange
                    } else if (secondsRemaining == 1) {
                        countdownText.setText("1");
                        countdownText.setTextColor(Color.parseColor("#76bbd2")); // Light blue
                    } else {
                        countdownText.setText("Start!");
                        countdownText.setTextColor(Color.parseColor("#4CAF50")); // Green
                    }
                    animateScale();
                });
            }

            public void onFinish() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    countdownText.setText(""); // Hide countdown text
                    startGame(); // Start the game
                }, 1000);
            }
        }.start();
    }

    private void animateScale() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.7f, 1.3f,  // X: from 70% to 130%
                0.7f, 1.3f,  // Y: from 70% to 130%
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X: center
                Animation.RELATIVE_TO_SELF, 0.5f  // Pivot Y: center
        );
        scaleAnimation.setDuration(600);
        scaleAnimation.setFillAfter(true);
        countdownText.startAnimation(scaleAnimation);
    }

    // Made public so that GamePanel can call it when the player taps "Play Again".
    public void startGame() {
        runOnUiThread(() -> {
            gameContainer.removeAllViews(); // Remove any existing views (countdown or previous game)
            gameContainer.addView(new GamePanel(this)); // Add a new GamePanel to start the game
        });
    }
}
