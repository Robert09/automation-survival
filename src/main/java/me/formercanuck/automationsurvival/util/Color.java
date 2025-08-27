package me.formercanuck.automationsurvival.util;

import org.joml.Vector3f;

public class Color {

    private Vector3f color;

    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color BROWN = new Color(139, 69, 19);
    public static final Color YELLOW = new Color(255, 255, 0);

    public Color(int r, int g, int b) {
        this.color = new Vector3f(r / 255f, g / 255f, b / 255f);
    }

    public float[] toArray() {
        return new float[]{color.x, color.y, color.z};
    }
}
