package com.kh.beatbot.ui.mesh;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.ui.color.Colors;

public class KnobShape extends Shape {
	private static final int NUM_FILL_VERTICES = 128 * 3;

	class Vertex {
		float x, y;

		Vertex(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private List<Vertex> tempVertices = new ArrayList<Vertex>();
	private int colorChangeVertex = 0;

	public KnobShape(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, NUM_FILL_VERTICES, 0);
	}

	@Override
	protected void updateVertices() {
		initTempVertices();

		for (int i = 2; i < tempVertices.size(); i++) {
			vertex(i - 2);
			vertex(i - 1);
			vertex(i);
		}
	}

	public void setLevel(float level) {
		colorChangeVertex = (int) (NUM_FILL_VERTICES * level) / 3;
		if ((colorChangeVertex & 1) == 0) { // even
			colorChangeVertex += 1; // cci should be odd
		}
		resetIndices();
		updateVertices();
	}

	private void initTempVertices() {
		if (!tempVertices.isEmpty())
			return;
		float theta = 3 * ¹ / 4; // start at 1/8 around the circle
		for (int i = 0; i < NUM_FILL_VERTICES / 6; i++) {
			// theta will range from ¹/4 to 7¹/8,
			// with the ¹/8 gap at the "bottom" of the view
			theta += 9 * ¹ / NUM_FILL_VERTICES;
			// main circles will show when user is not touching
			tempVertex(theta, width / 2.3f);
			tempVertex(theta, width / 3.3f);
		}
	}

	private void tempVertex(float theta, float radius) {
		tempVertices.add(new Vertex(x + (float) Math.cos(theta) * radius
				+ width / 2, y + (float) Math.sin(theta) * radius + width / 2));
	}

	private void vertex(int tempIndex) {
		fillVertex(tempVertices.get(tempIndex).x,
				tempVertices.get(tempIndex).y,
				getColor(tempIndex <= colorChangeVertex));
	}

	private float[] getColor(boolean selected) {
		return selected ? getFillColor() : Colors.VIEW_BG;
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		tempVertices.clear();
		super.layout(x, y, width, height);
	}
}
