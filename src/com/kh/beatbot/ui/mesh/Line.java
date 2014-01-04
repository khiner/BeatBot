package com.kh.beatbot.ui.mesh;

public class Line extends Shape {

	public Line(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor);
	}

	@Override
	protected int getNumFillVertices() {
		return 0;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 2;
	}

	@Override
	protected void updateVertices() {
		if (width > height) {
			strokeVertex(x, y);
			strokeVertex(x + width, y);
		} else {
			strokeVertex(x, y);
			strokeVertex(x, y + height);
		}
	}
}
