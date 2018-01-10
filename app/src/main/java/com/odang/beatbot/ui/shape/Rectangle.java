package com.odang.beatbot.ui.shape;

public class Rectangle extends Shape {
    public static final int NUM_FILL_VERTICES = 4;
    public static final int NUM_STROKE_VERTICES = 4;

    public static final short[] FILL_INDICES = {0, 0, 1, 3, 2, 2};
    public static final short[] STROKE_INDICES = {0, 1, 1, 2, 2, 3, 3, 0};

    public Rectangle(RenderGroup group, float[] fillColor, float[] strokeColor) {
        super(group, fillColor, strokeColor, FILL_INDICES, STROKE_INDICES, NUM_FILL_VERTICES,
                NUM_STROKE_VERTICES);
    }

    /********
     * ^--^ * |1/| * |/2| * ^--^ *
     ********/
    @Override
    protected void updateVertices() {
        fillVertex(x, y);
        fillVertex(x, y + height);
        fillVertex(x + width, y + height);
        fillVertex(x + width, y);

        // outline
        strokeVertex(x, y);
        strokeVertex(x, y + height);
        strokeVertex(x + width, y + height);
        strokeVertex(x + width, y);
    }
}
