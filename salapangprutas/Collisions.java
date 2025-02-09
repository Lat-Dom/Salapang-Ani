package com.lim.salapangprutas;

import java.util.ArrayList;

public class Collisions {
    public static void checkCollisions(ArrayList<rndSqr> squares, int screenWidth) {
        for (rndSqr square : squares) {
            if (square.pos.x <= 0 || square.pos.x + square.size >= screenWidth) {
                square.dx = -square.dx;
            }
        }
    }
}
