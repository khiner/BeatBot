package com.kh.beatbot.ui.mesh;

public class RoundedRect extends Shape {
	public static final short[] FILL_INDICES = getFillIndices();
	public static final short[] STROKE_INDICES = getStrokeIndices();

	public static final int NUM_CORNER_VERTICES = 6;

	public float roundThresh = 0;
	public float cornerRadius = -1;

	public RoundedRect(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, STROKE_INDICES);
	}

	private static short[] getFillIndices() {
		short[] fillIndices = new short[(NUM_CORNER_VERTICES * 4 + 1) * 3];

		for (int i = 0; i < NUM_CORNER_VERTICES * 4; i++) {
			fillIndices[i * 3] = 0; // first is center
			fillIndices[i * 3 + 1] = (short) i;
			fillIndices[i * 3 + 2] = (short) (i + 1);
		}

		fillIndices[fillIndices.length - 3] = 0;
		fillIndices[fillIndices.length - 2] = fillIndices[fillIndices.length - 4];
		fillIndices[fillIndices.length - 1] = 1;

		return fillIndices;
	}

	private static short[] getStrokeIndices() {
		short[] strokeIndices = new short[NUM_CORNER_VERTICES * 4 * 2];

		for (int i = 0; i < NUM_CORNER_VERTICES * 4; i++) {
			strokeIndices[i * 2] = (short) i;
			strokeIndices[i * 2 + 1] = (short) (i + 1);
		}

		strokeIndices[strokeIndices.length - 2] = strokeIndices[strokeIndices.length - 3];
		strokeIndices[strokeIndices.length - 1] = 0;

		return strokeIndices;
	}

	protected int getNumFillVertices() {
		return NUM_CORNER_VERTICES * 4 + 1;
	}

	protected int getNumStrokeVertices() {
		return NUM_CORNER_VERTICES * 4;
	}

	protected synchronized void updateVertices() {
		fillVertex(x + width / 2, y + height / 2); // center

		roundThresh = cornerRadius / 20;
		float theta = 0, addX, addY, vertexX, vertexY;
		for (int i = 0; i < getNumFillVertices() - 1; i++) {
			if (theta < ¹ / 2) { // lower right
				addX = width - cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = addY = cornerRadius;
			} else { // upper right
				addX = width - cornerRadius;
				addY = cornerRadius;
			}

			vertexX = roundX((float) Math.cos(theta) * cornerRadius + addX + x);
			vertexY = roundY((float) Math.sin(theta) * cornerRadius + addY + y);

			fillVertex(vertexX, vertexY);
			strokeVertex(vertexX, vertexY);

			theta += 2 * ¹ / (getNumFillVertices() - 1);
		}
	}

	public synchronized void setCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		if (cornerRadius < 0) {
			cornerRadius = width > height ? height / 4 : width / 4;
		}
		super.layout(x, y, width, height);
	}

	private float roundX(float vertexX) {
		if (Math.abs(vertexX - x) < roundThresh) {
			return x;
		} else if (Math.abs(vertexX - width - x) < roundThresh) {
			return width + x;
		} else if (Math.abs(vertexX - x - width + cornerRadius) < roundThresh) {
			return x + width - cornerRadius;
		} else if (Math.abs(vertexX - x - cornerRadius) < roundThresh) {
			return x + cornerRadius;
		}

		return vertexX;
	}

	private float roundY(float vertexY) {
		if (Math.abs(vertexY - y) < roundThresh) {
			return y;
		} else if (Math.abs(vertexY - height - y) < roundThresh) {
			return height + y;
		} else if (Math.abs(vertexY - y - height + cornerRadius) < roundThresh) {
			return y + height - cornerRadius;
		} else if (Math.abs(vertexY - y - cornerRadius) < roundThresh) {
			return y + cornerRadius;
		}
		return vertexY;
	}
}
