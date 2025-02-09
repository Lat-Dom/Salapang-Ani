package com.lim.salapangprutas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

import java.util.Random;

public class rndSqr {
    private static final Random rnd = new Random();
    public PointF pos;
    int size;
    private Bitmap image;
    float dx;
    private float dy;
    private int points;
    private boolean penalty;

    public rndSqr(PointF pos, int size, Bitmap image, int speed) {
        this(pos, size, image, (float) (speed * Math.cos(Math.PI / 4 + rnd.nextFloat() * Math.PI / 2)),
                (float) (speed * Math.sin(Math.PI / 4 + rnd.nextFloat() * Math.PI / 2)), false);
    }

    public rndSqr(PointF pos, int size, Bitmap image, float dx, float dy, boolean penalty) {
        this.pos = pos;
        this.size = size;
        this.image = Bitmap.createScaledBitmap(image, size, size, false);
        this.dx = dx;
        this.dy = dy;
        this.penalty = penalty;
        this.points = penalty ? -5 : (size < 100 ? 10 : 5);
    }

    public void update(int screenWidth, int screenHeight) {
        pos.x += dx;
        pos.y += dy;
    }

    public void draw(Canvas c) {
        c.drawBitmap(image, pos.x, pos.y, null);
    }

    public boolean contains(PointF point) {
        return point.x >= pos.x && point.x <= pos.x + size &&
                point.y >= pos.y && point.y <= pos.y + size;
    }

    public int getPoints() {
        return points;
    }

    public boolean isPenalty() {
        return penalty;
    }

    public boolean isPointSquare() {
        return !penalty;
    }
}
