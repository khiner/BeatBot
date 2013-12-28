package com.kh.beatbot.ui.mesh;


public class AdsrShape extends Shape {

	Circle[] circles = new Circle[4];

	protected AdsrShape(ShapeGroup group, float[] fillColor,
			float[] strokeColor) {
		super(group, fillColor, strokeColor);
		for (int i = 0; i < circles.length; i++) {
			circles[i] = (Circle) Shape.get(Type.CIRCLE, group, fillColor, null);
		}
	}

	@Override
	protected int getNumFillVertices() {
		return 0;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 10;
	}

	@Override
	protected void updateVertices() {

	}

	public void update(float[] vertices) {
		resetIndices();
		for (int i = 0; i < vertices.length / 2; i++) {
			if (i != 3) {
				circles[i < 3 ? i : i - 1].setPosition(vertices[i * 2], vertices[i * 2 + 1]);
			}
			if (i < (vertices.length - 1) / 2) {
				strokeVertex(vertices[i * 2], vertices[i * 2 + 1]);
				strokeVertex(vertices[(i + 1) * 2], vertices[(i + 1) * 2 + 1]);
			}
		}
	}
	
	public synchronized void layout(float x, float y, float width, float height) {
		super.layout(x, y, width, height);
		for (Circle circle : circles) {
			circle.setDimensions(width / 30, width / 30);
		}
	}
}
