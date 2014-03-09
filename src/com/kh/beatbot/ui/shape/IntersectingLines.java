package com.kh.beatbot.ui.shape;


public class IntersectingLines extends Shape {

	float intersectX = 0, intersectY = 0;

	public IntersectingLines(ShapeGroup group, float[] fillColor,
			float[] strokeColor) {
		super(group, fillColor, strokeColor, 0, 4);
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
