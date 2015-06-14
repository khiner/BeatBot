package com.kh.beatbot.ui.shape;

public class IntersectingLines extends Shape {
	float intersectX = 0, intersectY = 0;
	public static final int NUM_FILL_VERTICES = 8;

	public static final short[] FILL_INDICES = { 0, 0, 1, 3, 2, 2, 4, 4, 5, 7, 6, 6 };

	public IntersectingLines(RenderGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, null, FILL_INDICES, null, NUM_FILL_VERTICES, 0);
	}

	@Override
	protected void updateVertices() {
		float strokeWidth = height / 124;
		float halfStrokeWidth = strokeWidth / 2;

		fillVertex(x + intersectX - halfStrokeWidth, y);
		fillVertex(x + intersectX - halfStrokeWidth, y + height);
		fillVertex(x + intersectX + halfStrokeWidth, y + height);
		fillVertex(x + intersectX + halfStrokeWidth, y);

		fillVertex(x, y + intersectY - halfStrokeWidth);
		fillVertex(x, y + intersectY + halfStrokeWidth);
		fillVertex(x + width, y + intersectY + halfStrokeWidth);
		fillVertex(x + width, y + intersectY - halfStrokeWidth);
	}

	public void setIntersect(float intersectX, float intersectY) {
		this.intersectX = intersectX;
		this.intersectY = intersectY;
		update();
	}
}
