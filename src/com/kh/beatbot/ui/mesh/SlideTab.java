package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.view.page.Page;

public class SlideTab extends Shape {

	private float cornerRadius = 7;

	public SlideTab(ShapeGroup group, float[] fillColor) {
		super(group, new Mesh2D(16 * 4 * 3 * 2, fillColor));
	}

	@Override
	protected void createVertices(float[] fillColor) {
		createVertices(fillColor, null);
	}

	@Override
	protected void createVertices(float[] fillColor, float[] outlineColor) {
		float parentWidth = Page.mainPage.width;
		float parentHeight = Page.mainPage.height;

		RoundedRect roundedRect = new RoundedRect(null, fillColor);		
		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x + parentWidth - cornerRadius * 2, y, width, height);
		
		float[] vertices = roundedRect.fillMesh.vertices;
		for (int i = 0; i < vertices.length / 2; i++) {
			fillMesh.vertex(vertices[i * 2], vertices[i * 2 + 1]);
		}

		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x, y, parentWidth, parentHeight - y);

		for (int i = 0; i < roundedRect.fillMesh.vertices.length / 2; i++) {
			fillMesh.vertex(roundedRect.fillMesh.vertices[i * 2], roundedRect.fillMesh.vertices[i * 2 + 1]);
		}

		fillMesh.setColor(fillColor);
	}
}
