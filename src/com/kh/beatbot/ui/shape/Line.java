package com.kh.beatbot.ui.shape;


public class Line extends Shape {

	public Line(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, 0, 2);
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
