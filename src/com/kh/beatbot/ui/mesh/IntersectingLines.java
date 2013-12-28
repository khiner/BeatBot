package com.kh.beatbot.ui.mesh;

public class IntersectingLines extends Shape {

	float intersectX = 0, intersectY = 0;

	protected IntersectingLines(ShapeGroup group, float[] fillColor,
			float[] strokeColor) {
		super(group, fillColor, strokeColor);
	}

	@Override
	protected int getNumFillVertices() {
		return 0;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 4;
	}

	@Override
	protected void updateVertices() {
		strokeVertex(this.x + intersectX, this.y);
		strokeVertex(this.x + intersectX, this.y + height);
		strokeVertex(this.x, this.y + intersectY);
		strokeVertex(this.x + width, this.y + intersectY);
	}

	public void setIntersect(float intersectX, float intersectY) {
		this.intersectX = intersectX;
		this.intersectY = intersectY;
		update();
	}
}