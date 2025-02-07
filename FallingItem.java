package com.lim.salapangprutas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;

public class FallingItem {
    private PointF pos;
    private int size;
    private Bitmap image;
    private float dx, dy;
    private int points;
    private boolean penalty;
    private float rotation = 0;

    public FallingItem(PointF pos, int size, Bitmap image, float dx, float dy, boolean penalty) {
        this.pos = pos;
        this.size = size;
        // Scale the bitmap to the desired size
        this.image = Bitmap.createScaledBitmap(image, size, size, false);
        this.dx = dx;
        this.dy = dy;
        this.penalty = penalty;
        // Normal falling items: +1 point if not penalty, -1 if penalty.
        this.points = penalty ? -1 : 1;
    }

    // Setter for points so subclasses can override the default
    public void setPoints(int points) {
        this.points = points;
    }

    // Update the object's position and rotation.
    public void update(int screenWidth, int screenHeight) {
        pos.x += dx;
        pos.y += dy;
        rotation += 5; // Rotate the object for a nice visual effect

        // Bounce off the left/right boundaries.
        if (pos.x <= 0 || pos.x + size >= screenWidth) {
            dx = -dx;
        }
    }

    // Draw the object with its current rotation.
    public void draw(Canvas canvas) {
        Matrix matrix = new Matrix();
        matrix.postTranslate(-size / 2f, -size / 2f);
        matrix.postRotate(rotation);
        matrix.postTranslate(pos.x + size / 2f, pos.y + size / 2f);
        canvas.drawBitmap(image, matrix, null);
    }

    // Collision detection: returns true if the given point is within this object's bounds.
    public boolean contains(PointF point) {
        return (point.x >= pos.x && point.x <= pos.x + size &&
                point.y >= pos.y && point.y <= pos.y + size);
    }

    public int getPoints() {
        return points;
    }

    public boolean isPenalty() {
        return penalty;
    }

    // Returns true if the object has fallen below the bottom of the screen.
    public boolean isOffScreen(int screenHeight) {
        return pos.y > screenHeight;
    }
}
