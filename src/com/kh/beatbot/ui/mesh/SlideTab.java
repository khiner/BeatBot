package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.View;

public class SlideTab extends Shape {
	private static final int NUM_FILL_VERTICES = RoundedRect.NUM_CORNER_VERTICES * 5 * 3 * 2;

	private static RoundedRect roundedRect = new RoundedRect(null,
			Colors.LABEL_SELECTED, null);

	private float cornerRadius = 12;

	public SlideTab(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, NUM_FILL_VERTICES, 0);
	}

	@Override
	protected void updateVertices() {
		if (View.mainPage == null)
			return;
		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x + View.mainPage.width - cornerRadius * 2, y,
				width, height);

		for (int i = 0; i < roundedRect.NUM_FILL_VERTICES; i++) {
			fillVertex(roundedRect.getFillVertexX(i),
					roundedRect.getFillVertexY(i));
		}

		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x, y, View.mainPage.width, View.mainPage.height - y);

		for (int i = 0; i < roundedRect.NUM_FILL_VERTICES; i++) {
			fillVertex(roundedRect.getFillVertexX(i),
					roundedRect.getFillVertexY(i));
		}

		float theta = ¹, addX, addY;
		float centerX = x + View.mainPage.width;
		float centerY = y + height;
		float lastX = 0, lastY = 0;
		for (int i = 0; i < RoundedRect.NUM_CORNER_VERTICES; i++) {
			addX = addY = cornerRadius;
			float vertexX = (float) Math.cos(theta) * cornerRadius + addX + x
					+ View.mainPage.width;
			float vertexY = (float) Math.sin(theta) * cornerRadius + addY + y
					+ height;
			if (lastX != 0 && lastY != 0) {
				fillVertex(vertexX, vertexY);
				fillVertex(lastX, lastY);
				fillVertex(centerX, centerY);
			}
			lastX = vertexX;
			lastY = vertexY;
			theta += ¹ / (RoundedRect.NUM_CORNER_VERTICES * 2);
		}
	}
}
