package com.lim.salapangprutas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private ArrayList<rndSqr> squares = new ArrayList<>();
    private Random rnd = new Random();
    private boolean running = true;
    private Handler handler = new Handler();
    private int score = 0;
    private int lives = 3;
    // Wave parameters (used for waves spawn)
    private int squaresPerWave = 3;
    private int waveSpeed = 5;
    private long gameStartTime;
    private long gameDuration = 2 * 60 * 1000; // 2 minutes
    private Bitmap background;

    private int[] imageResources = {
            R.drawable.banana,
            R.drawable.apple,
            R.drawable.cherry,
            R.drawable.mango,
            R.drawable.plum,
            R.drawable.strawberry,
            R.drawable.raspberry,
            R.drawable.orange
    };

    private int penaltyImageResource = R.drawable.worms;
    // Array of flower images (the +5 object)
    private int[] flowerImageResource = {
            R.drawable.flower1,
            R.drawable.flower2,
            R.drawable.flower3
    };

    public GamePanel(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
    }

    // Wave-based spawning: if there are no fruit-type objects present, spawn a wave.
    private void startWaves() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!running) return; // Exit if game is over

                long elapsedTime = System.currentTimeMillis() - gameStartTime;
                if (elapsedTime >= gameDuration || lives <= 0) {
                    running = false;
                    handler.removeCallbacksAndMessages(null);
                    showGameOverScreen(); // Call game over screen
                    return;
                }

                // Spawn a new wave if no point‑giving (non‑penalty) objects exist.
                if (squares.stream().noneMatch(rndSqr::isPointSquare)) {
                    spawnWave(squaresPerWave, waveSpeed);
                    waveSpeed += 2; // Increase speed for subsequent waves
                }

                updateSquares();
                render();
                handler.postDelayed(this, 50);
            }
        }, 50);
    }

    /**
     * Spawns a wave of objects.
     * For each of the numSquares:
     * - With a fixed probability, a penalties are spawned.
     * - Otherwise, a non-penalty is spawned:
     *    • 10% chance for a flower (worth +5)
     *    • Otherwise a fruit (worth +1)
     */
    private void spawnWave(int numSquares, int speed) {
        for (int i = 0; i < numSquares; i++) {
            int x = rnd.nextInt(getWidth() - 100);
            x = Math.max(x, 0);
            PointF pos = new PointF(x, 0);
            int size = 150;
            boolean isPenalty = (rnd.nextInt(8) == 0);  // Same probability for worm as previous code.
            Bitmap image;
            if (isPenalty) {
                image = BitmapFactory.decodeResource(getResources(), penaltyImageResource);
                squares.add(new rndSqr(pos, size, image, 0, speed, true));
            } else {
                // For non-penalty objects, decide between a fruit and a flower.
                if (rnd.nextDouble() < 0.1) { // 10% chance to spawn a flower
                    int flowerIndex = rnd.nextInt(flowerImageResource.length);
                    image = BitmapFactory.decodeResource(getResources(), flowerImageResource[flowerIndex]);
                    rndSqr obj = new rndSqr(pos, size, image, speed);
                    obj.setPoints(5); // Flower awards +5
                    squares.add(obj);
                } else { // Otherwise, spawn a fruit.
                    int imageResId = imageResources[rnd.nextInt(imageResources.length)];
                    image = BitmapFactory.decodeResource(getResources(), imageResId);
                    rndSqr obj = new rndSqr(pos, size, image, speed);
                    obj.setPoints(1); // Fruit awards +1
                    squares.add(obj);
                }
            }
        }
    }

    private void updateSquares() {
        Iterator<rndSqr> iterator = squares.iterator();
        while (iterator.hasNext()) {
            rndSqr square = iterator.next();
            square.update(getWidth(), getHeight());
            // If the square falls below the bottom of the screen...
            if (square.pos.y > getHeight()) {
                // ...and if it is a fruit or flower (non-penalty), subtract one life.
                if (!square.isPenalty()) {
                    lives--;
                }
                iterator.remove();
            }
        }
        // Call collision checking with both screen width and height.
        Collisions.checkCollisions(squares, getWidth(), getHeight());
    }

    private void render() {
        Canvas c = holder.lockCanvas();
        if (c != null) {
            c.drawBitmap(background, 0, 0, null);
            for (rndSqr square : squares) {
                square.draw(c);
            }

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            c.drawText("Score: " + score, 20, 60, paint);
            c.drawText("Lives: " + lives, 20, 120, paint);

            long elapsedTime = System.currentTimeMillis() - gameStartTime;
            long timeRemaining = Math.max(0, gameDuration - elapsedTime);
            String timeText = String.format("%02d:%02d", (timeRemaining / 60000) % 60, (timeRemaining / 1000) % 60);

            Paint timerPaint = new Paint();
            timerPaint.setColor(Color.YELLOW);
            timerPaint.setTextSize(70);
            timerPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText(timeText, getWidth() / 2, 60, timerPaint);

            holder.unlockCanvasAndPost(c);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE) {
            PointF touchPos = new PointF(event.getX(), event.getY());
            Iterator<rndSqr> iterator = squares.iterator();
            while (iterator.hasNext()) {
                rndSqr square = iterator.next();
                if (square.contains(touchPos)) {
                    if (square.isPenalty()) {
                        lives--;
                    } else {
                        score += square.getPoints();
                    }
                    iterator.remove();
                }
            }
        }
        return true;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        running = true;
        gameStartTime = System.currentTimeMillis();
        // Ensure the background fits the screen size.
        background = Bitmap.createScaledBitmap(background, getWidth(), getHeight(), false);
        startWaves();
        render();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // No additional handling required lol XD.
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        running = false;
        handler.removeCallbacksAndMessages(null); // Stop all pending game updates.
    }

    /**
     * Displays a game-over overlay by inflating the game_over.xml layout. This overlay
     * shows the centered final score and two buttons: "Play Again" (which loops back to
     * the main activity) and "Main Menu" (placeholder for future main menu functionality). - Add it when completed
     */
    private void showGameOverScreen() {
        post(new Runnable() {
            @Override
            public void run() {
                if (getContext() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getContext();
                    FrameLayout gameContainer = activity.findViewById(R.id.gameContainer);
                    // Remove the current game view.
                    gameContainer.removeAllViews();

                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View gameOverView = inflater.inflate(R.layout.game_over, null);

                    // Update the final score text.
                    TextView scoreText = gameOverView.findViewById(R.id.finalScoreTextView);
                    scoreText.setText("Final Score: " + score);

                    // Set up the "Play Again" button.
                    Button playAgainButton = gameOverView.findViewById(R.id.playAgainButton);
                    playAgainButton.setOnClickListener(v -> {
                        // Restart the game immediately.
                        activity.startGame();
                    });

                    // Set up the "Main Menu" button.
                    Button mainMenuButton = gameOverView.findViewById(R.id.mainMenuButton);
                    mainMenuButton.setOnClickListener(v -> {
                        // Placeholder for future main menu functionality. - Add the main menu activity here thx
                        //activity.openMainMenu();
                    });

                    // Add the game-over view to the game container.
                    gameContainer.addView(gameOverView);
                }
            }
        });
    }
}
