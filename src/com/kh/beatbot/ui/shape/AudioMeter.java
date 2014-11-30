package com.kh.beatbot.ui.shape;

import com.kh.beatbot.ui.color.Color;

public class AudioMeter extends Shape {
	public static final int NUM_FILL_VERTICES = 300;
	public static final short[] FILL_INDICES = getFillIndices();
	private static final int WARNING_VERTEX_INDEX = 100;
	private static final int CLIPPING_VERTEX_INDEX = 200;
	private int levelVertex = 0;

	public AudioMeter(RenderGroup group, float[] fillColor, float[] strokeColor) {
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
		float x = this.x;
		for (float i = 0; i < NUM_FILL_VERTICES / 2; i++) {
			fillVertex(x, y);
			fillVertex(x, y + height);
			x += 2 * width / NUM_FILL_VERTICES;
		}
	}

	public void setLevel(final float level) {
		// Only see channel level changing if the 'spike' is greater than the current level
		levelVertex = Math.max(levelVertex, (int) (NUM_FILL_VERTICES * level));
		if ((levelVertex & 1) == 1) { // odd
			levelVertex += 1; // ccv should be even
		}
		updateFillColors();
	}

	public void tick() {
		// dampen level to emulate physical level meter
		levelVertex = Math.max(0, levelVertex - 2);
		updateFillColors();
	}

	private void updateFillColors() {
		for (int vertexIndex = 0; vertexIndex < NUM_FILL_VERTICES; vertexIndex++) {
			if (vertexIndex >= levelVertex) {
				setFillColor(vertexIndex, Color.VIEW_BG);
			} else if (vertexIndex < WARNING_VERTEX_INDEX) {
				setFillColor(vertexIndex, Color.GREEN);
			} else if (vertexIndex < CLIPPING_VERTEX_INDEX) {
				setFillColor(vertexIndex, Color.YELLOW);
			} else {
				setFillColor(vertexIndex, Color.RED);
			}
		}
	}
}
