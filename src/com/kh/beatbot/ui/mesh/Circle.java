package com.kh.beatbot.ui.mesh;

public class Circle extends Shape {
	public static final float � = (float) Math.PI, CIRCLE_RADIUS = 100;

	protected Circle(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor);
	}

	@Override
	protected int getNumFillVertices() {
		return 24 * 3;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 0;
	}

	@Override
	protected void updateVertices() {
		float theta = 0;
		float lastX = -1;
		float lastY = -1;
		for (int i = 0; i < getNumFillVertices() / 3 + 1; i++) {
			theta = (float) (2 * i * Math.PI / (getNumFillVertices() / 3));
			float x = (float) Math.cos(theta) * width / 2 + this.x + width / 2;
			float y = (float) Math.sin(theta) * height / 2 + this.y + height / 2;
			if (lastX != -1 && lastY != -1) {
				fillVertex(lastX, lastY);
				fillVertex(this.x + width / 2, this.y + height / 2);
				fillVertex(x, y);
			}
			lastX = x;
			lastY = y;
		}
	}
}
