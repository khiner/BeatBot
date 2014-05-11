package com.kh.beatbot.ui.shape;

public class AdsrShape extends Shape {
	private final Circle[] circles = new Circle[4];

	public AdsrShape(RenderGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, 0, 10);
		for (int i = 0; i < circles.length; i++) {
			circles[i] = new Circle(group, fillColor, null);
		}
	}

	@Override
	protected void updateVertices() {

	}

	public void update(float[] vertices) {
		resetIndices();
		for (int i = 0; i < vertices.length / 2; i++) {
			if (i != 3) {
				circles[i < 3 ? i : i - 1]
						.setPosition(vertices[i * 2] + x, vertices[i * 2 + 1] + y);
			}
			if (i < (vertices.length - 1) / 2) {
				strokeVertex(vertices[i * 2] + x, vertices[i * 2 + 1] + y);
				strokeVertex(vertices[(i + 1) * 2] + x, vertices[(i + 1) * 2 + 1] + y);
			}
		}
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		super.layout(x, y, width, height);
		for (Circle circle : circles) {
			circle.setDimensions(width / 30, width / 30);
		}
	}

	@Override
	public void show() {
		super.show();
		if (null != circles) {
			for (Circle circle : circles) {
				circle.show();
			}
		}
	}

	@Override
	public void hide() {
		super.hide();
		if (null != circles) {
			for (Circle circle : circles) {
				circle.hide();
			}
		}
	}
}
