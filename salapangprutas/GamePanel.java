package com.lim.salapangprutas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.AudioAttributes;


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
    private int squaresPerWave = 3;
    private int waveSpeed = 5;
    private long gameStartTime;
    private long gameDuration = 2 * 60 * 1000;
    private Bitmap background;


    private int[] imageResources = {
            R.drawable.banana,
            R.drawable.apple,
            R.drawable.cherry,
            R.drawable.mango,
            R.drawable.plum,
            R.drawable.strawberry,
            R.drawable.raspberry
    };

    private int penaltyImageResource = R.drawable.worms;

    public GamePanel(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
    }


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

                if (squares.stream().noneMatch(rndSqr::isPointSquare)) {
                    spawnWave(squaresPerWave, waveSpeed);
                    waveSpeed += 2;
                }

                updateSquares();
                render();
                handler.postDelayed(this, 50);
            }
        }, 50);
    }

    private void spawnWave(int numSquares, int speed) {
        for (int i = 0; i < numSquares; i++) {
            int x = rnd.nextInt(getWidth() - 100);
            x = Math.max(x, 0);

            PointF pos = new PointF(x, 0);
            int size = 150;

            boolean isPenalty = rnd.nextInt(8) == 0;
            Bitmap image;

            if (isPenalty) {
                image = BitmapFactory.decodeResource(getResources(), penaltyImageResource);
                squares.add(new rndSqr(pos, size, image, 0, speed, true));
            } else {
                int imageResId = imageResources[rnd.nextInt(imageResources.length)];
                image = BitmapFactory.decodeResource(getResources(), imageResId);
                squares.add(new rndSqr(pos, size, image, speed));
            }
        }
    }

    private void updateSquares() {
        Iterator<rndSqr> iterator = squares.iterator();
        while (iterator.hasNext()) {
            rndSqr square = iterator.next();
            square.update(getWidth(), getHeight());

            if (square.pos.y > getHeight()) {
                // If the falling object is a point-giving square (i.e. not a penalty),
                // then the player loses a life.
                if (!square.isPenalty()) {
                    lives--;
                }
                iterator.remove();
            }
        }
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
            paint.setColor(android.graphics.Color.WHITE);
            paint.setTextSize(50);
            c.drawText("Score: " + score, 20, 60, paint);
            c.drawText("Lives: " + lives, 20, 120, paint);

            long elapsedTime = System.currentTimeMillis() - gameStartTime;
            long timeRemaining = Math.max(0, gameDuration - elapsedTime);
            String timeText = String.format("%02d:%02d", (timeRemaining / 60000) % 60, (timeRemaining / 1000) % 60);

            Paint timerPaint = new Paint();
            timerPaint.setColor(android.graphics.Color.YELLOW);
            timerPaint.setTextSize(70);
            timerPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText(timeText, getWidth() / 2, 60, timerPaint);

            holder.unlockCanvasAndPost(c);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
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

        // Ensures background fits the screen
        background = Bitmap.createScaledBitmap(background, getWidth(), getHeight(), false);

        startWaves();
        render();
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        running = false;
        handler.removeCallbacksAndMessages(null); // Stop all pending game updates
    }

    private void showGameOverScreen() {
        post(new Runnable() {
            @Override
            public void run() {
                // Ensure that the context is an instance of MainActivity
                if (getContext() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getContext();
                    // Obtain the game container from MainActivity (it should have the id 'gameContainer')
                    FrameLayout gameContainer = activity.findViewById(R.id.gameContainer);
                    // Remove the game view (this GamePanel) from the container
                    gameContainer.removeAllViews();

                    // Create an overlay layout for the game over screen
                    LinearLayout gameOverLayout = new LinearLayout(activity);
                    gameOverLayout.setOrientation(LinearLayout.VERTICAL);
                    gameOverLayout.setGravity(Gravity.CENTER);
                    FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT);
                    gameOverLayout.setLayoutParams(overlayParams);

                    // Create the "GAME OVER!" TextView
                    TextView gameOverText = new TextView(activity);
                    gameOverText.setText("GAME OVER!");
                    gameOverText.setTextColor(Color.RED);
                    gameOverText.setTextSize(80);
                    gameOverText.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    textParams.setMargins(0, 0, 0, 50); // margin bottom for spacing
                    gameOverText.setLayoutParams(textParams);

                    // Create the final score TextView (score is a field in GamePanel)
                    TextView scoreText = new TextView(activity);
                    scoreText.setText("Final Score: " + score);
                    scoreText.setTextColor(Color.WHITE);
                    scoreText.setTextSize(50);
                    scoreText.setGravity(Gravity.CENTER);
                    scoreText.setLayoutParams(textParams);

                    // Create the "Play Again" button
                    Button playAgainButton = new Button(activity);
                    playAgainButton.setText("Play Again");
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    playAgainButton.setLayoutParams(buttonParams);
                    playAgainButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Call MainActivity's startGame() method to restart the game.
                            // (Make sure startGame() is public in MainActivity.)
                            activity.startGame();
                        }
                    });

                    // Add the views to the overlay layout
                    gameOverLayout.addView(gameOverText);
                    gameOverLayout.addView(scoreText);
                    gameOverLayout.addView(playAgainButton);

                    // Add the overlay layout to the game container
                    gameContainer.addView(gameOverLayout);
                }
            }
        });
    }

}