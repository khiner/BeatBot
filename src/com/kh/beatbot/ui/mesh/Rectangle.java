package com.kh.beatbot.ui.mesh;

public class Rectangle extends Shape {
	public static final short[] FILL_INDICES = { 0, 1, 2, 2, 3, 0 };
	public static final short[] STROKE_INDICES = { 0, 1, 1, 2, 2, 3, 3, 0 };

	public Rectangle(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, STROKE_INDICES);
	}

	protected int getNumFillVertices() {
		return 4;
	}

	protected int getNumStrokeVertices() {
		return 4;
	}

	/********
	 * ^--^ * |1/| * |/2| * ^--^ *
	 ********/
	protected synchronized void updateVertices() {
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
