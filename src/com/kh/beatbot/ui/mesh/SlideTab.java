package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.view.page.Page;

public class SlideTab extends Shape {

	public SlideTab(ShapeGroup group, float[] fillColor) {
		super(group, new Mesh2D(12, fillColor));
	}

	public SlideTab(ShapeGroup group, float[] fillColor, float[] outlineColor) {
		super(group, new Mesh2D(12, fillColor), new Mesh2D(12, outlineColor));
	}

	@Override
	protected void createVertices(float[] fillColor) {
		createVertices(fillColor, null);
	}

	@Override
	protected void createVertices(float[] fillColor, float[] outlineColor) {
		float parentWidth = Page.mainPage.width;
		float parentHeight = Page.mainPage.height;

		float[][] vertices = {
				{ x + width + parentWidth, 0 },
				{ x + parentWidth, 0 },
				{ x + width + parentWidth, height },

				{ x + parentWidth, 0 },
				{ x + width + parentWidth, height },
				{ x + parentWidth, height },

				{ x + parentWidth, 0 },
				{ x, 0 },
				{ x + parentWidth, parentHeight },

				{ x, 0 },
				{ x + parentWidth, parentHeight },
				{ x, parentHeight }
		};

		for (float[] vertex : vertices) {
			fillMesh.vertex(vertex[0], vertex[1]);
			outlineMesh.vertex(vertex[0], vertex[1]);
		}

		fillMesh.setColor(fillColor);
		if (outlineColor != null) {
			outlineMesh.setColor(outlineColor);
		}
	}
}
