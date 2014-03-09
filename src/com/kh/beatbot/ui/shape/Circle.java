package com.kh.beatbot.ui.shape;


public class Circle extends Shape {
	public static final float ¹ = (float) Math.PI, CIRCLE_RADIUS = 100;

	public static final short[] FILL_INDICES = getFillIndices();

	private static final int NUM_FILL_VERTICES = 18;

	public Circle(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, null,
				NUM_FILL_VERTICES, 0);
	}

	private static short[] getFillIndices() {
		short[] fillIndices = new short[(NUM_FILL_VERTICES + 1) * 2 + 1];

		fillIndices[0] = 0; // first is center
		fillIndices[1] = 0; // first is center
		for (int i = 1; i < NUM_FILL_VERTICES; i++) {
			fillIndices[i * 2] = 0; // first is center
			fillIndices[i * 2 + 1] = (short) i;
		}

		fillIndices[fillIndices.length - 3] = 0;
		fillIndices[fillIndices.length - 2] = 1;
		fillIndices[fillIndices.length - 1] = 1;

		return fillIndices;
	}

	@Override
	protected void updateVertices() {
		fillVertex(this.x, this.y); // center

		float theta = 0;
		for (int i = 0; i < NUM_FILL_VERTICES - 1; i++) {
			float x = (float) Math.cos(theta) * width / 2 + this.x;
			float y = (float) Math.sin(theta) * height / 2 + this.y;
			fillVertex(x, y);
			theta += 2 * Math.PI / (NUM_FILL_VERTICES - 1);
		}
	}
}
