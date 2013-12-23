package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.View;

public class SlideTab extends Shape {

	private float cornerRadius = 12;
	private RoundedRect roundedRect;

	protected int getNumFillVertices() {
		return RoundedRect.NUM_CORNER_VERTICES * 5 * 3 * 2;
	}

	protected int getNumStrokeVertices() {
		return 0;
	}

	public SlideTab(ShapeGroup group) {
		super(group);
		roundedRect = (RoundedRect) Shape.get(Type.ROUNDED_RECT, group,
				Colors.LABEL_SELECTED, null);
	}

	@Override
	protected void updateVertices() {
		if (View.mainPage == null)
			return;
		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x + View.mainPage.width - cornerRadius * 2, y,
				width, height);

		for (int i = roundedRect.fillMesh.parentVertexIndex; i < roundedRect.fillMesh.numVertices; i++) {
			fillVertex(roundedRect.fillMesh.group.getVertexX(i),
					roundedRect.fillMesh.group.getVertexY(i));
		}

		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x, y, View.mainPage.width, View.mainPage.height - y);

		for (int i = roundedRect.fillMesh.parentVertexIndex; i < roundedRect.fillMesh.numVertices; i++) {
			fillVertex(roundedRect.fillMesh.group.getVertexX(i),
					roundedRect.fillMesh.group.getVertexY(i));
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
