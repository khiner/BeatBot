package com.kh.beatbot.ui.mesh;

import java.util.ArrayList;
import java.util.List;

public class KnobShape extends Shape {

	class Vertex {
		float x, y;

		Vertex(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private List<Vertex> tempVertices = new ArrayList<Vertex>();
	private float level = 1;

	public KnobShape(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor);
	}

	@Override
	protected int getNumFillVertices() {
		return 128 * 3;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 0;
	}

	@Override
	protected void updateVertices() {
		initTempVertices();
		for (int i = 2; i < tempVertices.size(); i++) {
			fillVertex(tempVertices.get(i - 2).x, tempVertices.get(i - 2).y);
			fillVertex(tempVertices.get(i - 1).x, tempVertices.get(i - 1).y);
			fillVertex(tempVertices.get(i).x, tempVertices.get(i).y);
		}
	}

	public void setLevel(float level) {
		this.level = level;
	}

	private void initTempVertices() {
		if (!tempVertices.isEmpty())
			return;
		float theta = 3 * ¹ / 4; // start at 1/8 around the circle
		for (int i = 0; i < getNumFillVertices() / 6; i++) {
			// theta will range from ¹/4 to 7¹/8,
			// with the ¹/8 gap at the "bottom" of the view
			theta += 4 * ¹ / getNumFillVertices();
			// main circles will show when user is not touching
			tempVertices.add(new Vertex((float) Math.cos(theta) * width / 2.3f
					+ width / 2, (float) Math.sin(theta) * width / 2.3f + width
					/ 2));
			tempVertices.add(new Vertex((float) Math.cos(theta) * width / 3.3f
					+ width / 2, (float) Math.sin(theta) * width / 3.3f + width
					/ 2));
		}
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		tempVertices.clear();
		super.layout(x, y, width, height);
	}
}
