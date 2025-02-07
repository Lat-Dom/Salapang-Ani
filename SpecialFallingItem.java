package com.lim.salapangprutas;

import android.graphics.Bitmap;
import android.graphics.PointF;

public class SpecialFallingItem extends FallingItem {

    public SpecialFallingItem(PointF pos, int size, Bitmap image, float dx, float dy) {
        // For special items:
        // - We always assume it's non-penalty (false).
        // - Multiply the speeds by 2 to make it fall faster.
        super(pos, size, image, dx * 2, dy * 2, false);
        // Set the points to 5.
        setPoints(5);
    }
}
