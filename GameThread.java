package com.lim.salapangprutas;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private GamePanel gamePanel;
    private SurfaceHolder surfaceHolder;
    private boolean running;
    private static final long FRAME_TIME = 50; // Approximately 20 FPS

    public GameThread(GamePanel gamePanel, SurfaceHolder surfaceHolder) {
        this.gamePanel = gamePanel;
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (running) {
            long startTime = System.currentTimeMillis();
            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gamePanel.updateGame();
                    if (canvas != null) {
                        gamePanel.render(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            long frameTime = System.currentTimeMillis() - startTime;
            long sleepTime = FRAME_TIME - frameTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // Handle the interruption if needed
                }
            }
        }
    }
}
