package com.lim.salapangprutas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    // Game state variables
    private int lives = 3;
    private int score = 0;
    private int fruitSize = 120;
    private float baseSpeed = 5f;
    private float fallSpeed = baseSpeed;
    private long lastSpawnTime = 0;
    private long spawnInterval = 350; // milliseconds
    private long gameStartTime;
    private long gameDuration = 3 * 60 * 1000; // 3 minutes

    // Increase speed every 15 seconds (15,000 milliseconds)
    private long speedIncreaseRate = 15000;

    // Bitmaps for fruits and penalty items
    private Bitmap bananaBitmap, jackfruitBitmap, radishBitmap, pineappleBitmap, penaltyBitmap;
    private Bitmap cherryBitmap, greenAppleBitmap, mangoBitmap, orangeBitmap, plumBitmap, strawberryBitmap, waterMelonBitmap;

    // List of falling items and random number generator
    private ArrayList<FallingItem> fallingItems;
    private Random rnd;

    // Game thread and state flag
    private GameThread gameThread;
    private boolean gameOver = false;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);

        fallingItems = new ArrayList<>();
        rnd = new Random();

        // Load your bitmaps from resources
        bananaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banana);
        jackfruitBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jackfruit);
        radishBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.radish);
        pineappleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pineapple);
        cherryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cherry);
        mangoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mango);
        orangeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.orange);
        greenAppleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.green_apple);
        plumBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plum);
        strawberryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.strawberry);
        waterMelonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermelon);
        penaltyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.worms);
    }

    // Return one of the fruit bitmaps at random
    private Bitmap getRandomFruitBitmap() {
        // With 11 fruit images (indices 0 to 10), choose from 0 to 10.
        int choice = rnd.nextInt(11);
        switch (choice) {
            case 0: return bananaBitmap;
            case 1: return jackfruitBitmap;
            case 2: return radishBitmap;
            case 3: return pineappleBitmap;
            case 4: return cherryBitmap;
            case 5: return mangoBitmap;
            case 6: return orangeBitmap;
            case 7: return greenAppleBitmap;
            case 8: return plumBitmap;
            case 9: return strawberryBitmap;
            case 10: return waterMelonBitmap;
            default: return bananaBitmap;
        }
    }

    // Spawn a new falling item (fruit or penalty)
    private void spawnFallingItem() {
        int x = rnd.nextInt(getWidth() - fruitSize);
        x = Math.max(x, 0);
        PointF pos = new PointF(x, 0);

        // Decide randomly whether to spawn a special item (1 in 20 chance)
        boolean spawnSpecial = rnd.nextInt(20) == 0;

        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        float currentFallSpeed = baseSpeed + (elapsedTime / (float) speedIncreaseRate);
        // Calculate a base speed value and add a random component.
        float speed = currentFallSpeed + rnd.nextFloat() * 3;
        float dx = 0, dy = speed;
        Bitmap image;

        if (spawnSpecial) {
            // For special items, use a fruit image (or a special image if you have one).
            image = getRandomFruitBitmap();
            // Calculate a random angle for movement.
            float angle = (float) (rnd.nextFloat() * Math.PI / 2 + Math.PI / 4);
            dx = (float) (speed * Math.cos(angle));
            dy = (float) (speed * Math.sin(angle));
            // Create and add a special falling item (its constructor will double its speed).
            FallingItem item = new SpecialFallingItem(pos, fruitSize, image, dx, dy);
            fallingItems.add(item);
        } else {
            // For normal items, decide if it's a penalty.
            boolean isPenalty = rnd.nextInt(8) == 0;
            image = isPenalty ? penaltyBitmap : getRandomFruitBitmap();

            if (!isPenalty) {
                float angle = (float) (rnd.nextFloat() * Math.PI / 2 + Math.PI / 4);
                dx = (float) (speed * Math.cos(angle));
                dy = (float) (speed * Math.sin(angle));
            }
            FallingItem item = new FallingItem(pos, fruitSize, image, dx, dy, isPenalty);
            fallingItems.add(item);
        }
    }

    // Update game logic: spawn items, update positions, remove off-screen objects, etc.
    public void updateGame() {
        if (gameOver) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= spawnInterval) {
            spawnFallingItem();
            lastSpawnTime = currentTime;
        }

        // Update each falling item
        for (FallingItem item : fallingItems) {
            item.update(getWidth(), getHeight());
        }

        // Remove items that have fallen off the screen.
        Iterator<FallingItem> iterator = fallingItems.iterator();
        while (iterator.hasNext()) {
            FallingItem item = iterator.next();
            if (item.isOffScreen(getHeight())) {
                if (!item.isPenalty()) {
                    lives--;
                    if (lives <= 0) {
                        endGame();
                    }
                }
                iterator.remove();
            }
        }

        // End the game if the time is up
        long elapsedTime = currentTime - gameStartTime;
        if (elapsedTime >= gameDuration) {
            endGame();
        }
    }

    // Render the game state to the canvas.
    public void render(Canvas canvas) {
        if (canvas == null) return;

        // Clear the screen
        canvas.drawColor(android.graphics.Color.BLACK);

        // Draw each falling item
        for (FallingItem item : fallingItems) {
            item.draw(canvas);
        }

        // Draw the score
        Paint scorePaint = new Paint();
        scorePaint.setColor(android.graphics.Color.WHITE);
        scorePaint.setTextSize(50);
        canvas.drawText("Score: " + score, 20, 60, scorePaint);

        // Draw the lives
        Paint livesPaint = new Paint();
        livesPaint.setColor(android.graphics.Color.RED);
        livesPaint.setTextSize(50);
        canvas.drawText("Lives: " + lives, getWidth() - 200, 60, livesPaint);

        // Draw the timer
        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        long timeRemaining = Math.max(0, gameDuration - elapsedTime);
        long minutes = (timeRemaining / 60000) % 60;
        long seconds = (timeRemaining / 1000) % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);

        Paint timerPaint = new Paint();
        timerPaint.setColor(android.graphics.Color.YELLOW);
        timerPaint.setTextSize(70);
        timerPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(timeText, getWidth() / 2, 60, timerPaint);
    }

    // Collision detection with the player's touch.
    // When the user touches the screen, we check if that point collides with any falling object.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver) return true;

        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE) {

            PointF touchPos = new PointF(event.getX(), event.getY());
            Iterator<FallingItem> iterator = fallingItems.iterator();
            while (iterator.hasNext()) {
                FallingItem item = iterator.next();
                // If the touch collides with this object...
                if (item.contains(touchPos)) {
                    if (item.isPenalty()) {
                        lives--;
                        iterator.remove();
                        if (lives <= 0) {
                            endGame();
                            return true;
                        }
                    } else {
                        score += item.getPoints();
                        iterator.remove();
                    }
                }
            }
        }
        return true;
    }

    // End the game and display game-over logic.
    private void endGame() {
        if (!gameOver) {
            gameOver = true;
            if (gameThread != null) {
                gameThread.setRunning(false);
            }
            post(this::showGameOverScreen);
        }
    }

    // Show the game-over screen (placeholder – replace with your own implementation)
    private void showGameOverScreen() {
        System.out.println("Game Over! Final Score: " + score);
    }

    // SurfaceHolder.Callback methods
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gameStartTime = System.currentTimeMillis();
        gameThread = new GameThread(this, getHolder());
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Handle changes here if needed.
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        if (gameThread != null) {
            gameThread.setRunning(false);
        }
        while (retry) {
            try {
                if (gameThread != null) {
                    gameThread.join();
                }
                retry = false;
            } catch (InterruptedException e) {
                // Retry shutting down the thread.
            }
        }
    }
}
