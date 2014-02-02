package com.kh.beatbot.ui.mesh;

public class Circle extends Shape {
	public static final float ¹ = (float) Math.PI, CIRCLE_RADIUS = 100;

	public static final short[] FILL_INDICES = getFillIndices();

	private static final int NUM_FILL_VERTICES = 18;

	public Circle(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, null);
	}

	private static short[] getFillIndices() {
		short[] fillIndices = new short[NUM_FILL_VERTICES * 3];

		for (int i = 0; i < NUM_FILL_VERTICES - 1; i++) {
			fillIndices[i * 3] = 0; // first is center
			fillIndices[i * 3 + 1] = (short) i;
			fillIndices[i * 3 + 2] = (short) (i + 1);
		}

		fillIndices[fillIndices.length - 3] = 0;
		fillIndices[fillIndices.length - 2] = fillIndices[fillIndices.length - 4];
		fillIndices[fillIndices.length - 1] = 1;

		return fillIndices;
	}

	@Override
	protected int getNumFillVertices() {
		return NUM_FILL_VERTICES;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 0;
	}

	@Override
	protected void updateVertices() {
		fillVertex(this.x, this.y); // center

		float theta = 0;
		for (int i = 0; i < getNumFillVertices() - 1; i++) {
			float x = (float) Math.cos(theta) * width / 2 + this.x;
			float y = (float) Math.sin(theta) * height / 2 + this.y;
			fillVertex(x, y);
			theta += 2 * Math.PI / (getNumFillVertices() - 1);
		}
	}
}
