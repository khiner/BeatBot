package com.kh.beatbot.ui.mesh;

public class RoundedRect extends Shape {
	public static final int NUM_CORNER_VERTICES = 6;
	public float roundThresh = 0;
	public float cornerRadius = -1;

	protected RoundedRect(ShapeGroup group) {
		super(group);
	}

	protected int getNumFillVertices() {
		return NUM_CORNER_VERTICES * 4 * 3;
	}

	protected int getNumStrokeVertices() {
		return NUM_CORNER_VERTICES * 4 * 2;
	}

	protected synchronized void updateVertices() {
		if (width == 0.0f || height == 0.0f) {
			return;
		}
		roundThresh = cornerRadius / 20;
		float theta = 0, addX, addY, vertexX, vertexY;
		float centerX = x + width / 2;
		float centerY = y + height / 2;
		float firstX = 0, firstY = 0, lastX = Float.MIN_VALUE, lastY = Float.MIN_VALUE;
		for (int i = 0; i < getNumFillVertices() / 3; i++) {
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
			if (lastX != Float.MIN_VALUE && lastY != Float.MIN_VALUE) {
				fillVertex(vertexX, vertexY);
				fillVertex(lastX, lastY);
				fillVertex(centerX, centerY);
				strokeVertex(vertexX, vertexY);
				strokeVertex(lastX, lastY);
			} else {
				firstX = vertexX;
				firstY = vertexY;
			}
			lastX = vertexX;
			lastY = vertexY;
			theta += 6 * ¹ / getNumFillVertices();
		}
		fillVertex(firstX, firstY);
		fillVertex(lastX, lastY);
		fillVertex(centerX, centerY);
		strokeVertex(firstX, firstY);
		strokeVertex(lastX, lastY);
	}

	public synchronized void setCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		if (cornerRadius < 0) {
			cornerRadius = width > height ? height / 5 : width / 5;
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
