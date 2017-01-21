package com.odang.beatbot.ui.shape;

import com.odang.beatbot.ui.color.Color;

public class AudioMeter extends Shape {
	public static final int NUM_FILL_VERTICES = 300;
	public static final short[] FILL_INDICES = getFillIndices();
	private static final int WARNING_VERTEX_INDEX = 100;
	private static final int CLIPPING_VERTEX_INDEX = 200;
	private float levelVertex = 0;

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
		setLevelVertex(Math.max(levelVertex, NUM_FILL_VERTICES * level));
	}

	public void tick() {
		// dampen level to emulate physical level meter
		setLevelVertex(levelVertex * 0.9f);
	}

	private int getAdjustedLevelVertex() {
		if (levelVertex < 0) {
			levelVertex = 0; // must be >= 0
		}

		int evenLevelIndex = (int) levelVertex;
		if ((evenLevelIndex & 1) == 1) { // odd
			evenLevelIndex += 1; // must be even
		}
		return evenLevelIndex;
	}

	private void setLevelVertex(float levelVertex) {
		int prevAdjustedLevelVertex = getAdjustedLevelVertex();
		this.levelVertex = levelVertex;
		int adjustedLevelVertex = getAdjustedLevelVertex();

		// only update fill colors of changed indices
		for (int vertexIndex = Math.min(prevAdjustedLevelVertex, adjustedLevelVertex); vertexIndex < Math
				.max(prevAdjustedLevelVertex, adjustedLevelVertex); vertexIndex++) {
			if (vertexIndex >= adjustedLevelVertex) {
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
