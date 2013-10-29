package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.view.page.Page;

public class SlideTab extends Shape {

	private float cornerRadius = 12;
	private RoundedRect roundedRect;

	public SlideTab(ShapeGroup group, float[] fillColor) {
		super(group, new Mesh2D(RoundedRect.NUM_CORNER_VERTICES * 5 * 3 * 2, fillColor));
		roundedRect = new RoundedRect(null, fillColor);
	}

	@Override
	protected void createVertices(float[] fillColor) {
		createVertices(fillColor, null);
	}

	@Override
	protected void createVertices(float[] fillColor, float[] outlineColor) {
		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x + Page.mainPage.width - cornerRadius * 2, y, width, height);

		float[] vertices = roundedRect.fillMesh.vertices;
		for (int i = 0; i < vertices.length / 2; i++) {
			fillMesh.vertex(vertices[i * 2], vertices[i * 2 + 1]);
		}

		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x, y, Page.mainPage.width, Page.mainPage.height - y);

		for (int i = 0; i < roundedRect.fillMesh.vertices.length / 2; i++) {
			fillMesh.vertex(roundedRect.fillMesh.vertices[i * 2],
					roundedRect.fillMesh.vertices[i * 2 + 1]);
		}

		float theta = ¹, addX, addY;
		float centerX = x + Page.mainPage.width;
		float centerY = y + height;
		float lastX = 0, lastY = 0;
		for (int i = 0; i < RoundedRect.NUM_CORNER_VERTICES; i++) {
			addX = addY = cornerRadius;
			float vertexX = (float) Math.cos(theta) * cornerRadius + addX + x + Page.mainPage.width;
			float vertexY = (float) Math.sin(theta) * cornerRadius + addY + y + height ;
			if (lastX != 0 && lastY != 0) {
				fillMesh.vertex(vertexX, vertexY);
				fillMesh.vertex(lastX, lastY);
				fillMesh.vertex(centerX, centerY);
			}
			lastX = vertexX;
			lastY = vertexY;
			theta += ¹ / (RoundedRect.NUM_CORNER_VERTICES * 2);
		}

		fillMesh.setColor(fillColor);
	}
}
