package com.kh.beatbot.ui.shape;

public class RoundedRect extends Shape {
	public static final short[] FILL_INDICES = getFillIndices();
	public static final short[] STROKE_INDICES = getStrokeIndices();

	public static final int NUM_CORNER_VERTICES = 6,
			NUM_FILL_VERTICES = NUM_CORNER_VERTICES * 4 + 1,
			NUM_STROKE_VERTICES = NUM_CORNER_VERTICES * 4;

	public float roundThresh = 0, cornerRadius = -1;

	public RoundedRect(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, STROKE_INDICES, NUM_FILL_VERTICES,
				NUM_STROKE_VERTICES);
	}

	private static short[] getFillIndices() {
		short[] fillIndices = new short[(NUM_STROKE_VERTICES + 1) * 2 + 1];

		for (short i = 0; i < NUM_STROKE_VERTICES + 1; i++) {
			// first two should be 0 to make degenerate triangles
			fillIndices[i * 2] = i;
			fillIndices[i * 2 + 1] = 0;
		}
		// degenerate triangle end
		fillIndices[fillIndices.length - 2] = 1;
		fillIndices[fillIndices.length - 1] = 1;

		return fillIndices;
	}

	private static short[] getStrokeIndices() {
		short[] strokeIndices = new short[NUM_STROKE_VERTICES * 2];

		for (int i = 0; i < NUM_STROKE_VERTICES; i++) {
			strokeIndices[i * 2] = (short) i;
			strokeIndices[i * 2 + 1] = (short) (i + 1);
		}

		strokeIndices[strokeIndices.length - 2] = strokeIndices[strokeIndices.length - 3];
		strokeIndices[strokeIndices.length - 1] = 0;

		return strokeIndices;
	}

	protected synchronized void updateVertices() {
		fillVertex(x + width / 2, y + height / 2); // center

		roundThresh = cornerRadius / 20;
		float theta = 0, addX, addY, vertexX, vertexY;
		for (int i = 0; i < NUM_STROKE_VERTICES; i++) {
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

			theta += 2 * ¹ / NUM_STROKE_VERTICES;
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
