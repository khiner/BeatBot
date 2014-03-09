package com.kh.beatbot.ui.shape;

import com.kh.beatbot.ui.color.Colors;

public class KnobShape extends Shape {
	private static final int NUM_FILL_VERTICES = 128;
	private static final short[] FILL_INDICES = getFillIndices();
	private int colorChangeVertex = 0;

	public KnobShape(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, null, NUM_FILL_VERTICES, 0);
	}

	private static short[] getFillIndices() {
		short[] fillIndices = new short[NUM_FILL_VERTICES + 2];

		fillIndices[0] = 0;
		for (int i = 1; i < NUM_FILL_VERTICES + 1; i++) {
			fillIndices[i] = (short) (i - 1);
		}
		fillIndices[fillIndices.length - 1] = fillIndices[fillIndices.length - 2];
		return fillIndices;
	}

	@Override
	protected void updateVertices() {
		float theta = 3 * ¹ / 4; // start at 1/8 around the circle
		for (int i = 0; i < NUM_FILL_VERTICES / 2; i++) {
			// theta will range from ¹/4 to 7¹/8,
			// with the ¹/8 gap at the "bottom" of the view
			theta += 3 * ¹ / NUM_FILL_VERTICES;
			// main circles will show when user is not touching
			vertex(i, theta, width / 2.3f);
			vertex(i, theta, width / 3.3f);
		}
	}

	public void setLevel(float level) {
		colorChangeVertex = (int) (NUM_FILL_VERTICES * level);
		if ((colorChangeVertex & 1) == 1) { // odd
			colorChangeVertex += 1; // ccv should be even
		}

		setFillColor(fillColor);
	}

	private void vertex(int i, float theta, float radius) {
		fillVertex(x + (float) Math.cos(theta) * radius + width / 2, y + (float) Math.sin(theta)
				* radius + width / 2);
	}

	public synchronized void setFillColor(float[] fillColor) {
		this.fillColor = fillColor;

		for (int vertexIndex = 0; vertexIndex < colorChangeVertex; vertexIndex++) {
			setFillColor(vertexIndex, getFillColor());
		}
		for (int vertexIndex = colorChangeVertex; vertexIndex < NUM_FILL_VERTICES; vertexIndex++) {
			setFillColor(vertexIndex, Colors.VIEW_BG);
		}
	}
}
