package com.kh.beatbot.view.mesh;

import com.kh.beatbot.global.Colors;

public class Rectangle extends Shape {
	public Rectangle(ShapeGroup group, float x, float y, float width,
			float height, float[] fillColor, float[] outlineColor) {
		// 6 vertices for rect fill (two triangles)
		// 8 vertices for rect outline (4 sides * 2 vertices per side)
		super(group, x, y, width, height, new Mesh2D(6), new Mesh2D(8));
		createVertices(fillColor, outlineColor);
	}


	public void update(float x, float y, float width, float height, float[] fillColor, float[] outlineColor) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		fillMesh.index = 0;
		outlineMesh.index = 0;
		createVertices(fillColor, outlineColor);
		group.update(this);
	}

	public void setColors(float[] fillColor, float[] outlineColor) {
		fillMesh.index = 0;
		outlineMesh.index = 0;
		createVertices(fillColor, outlineColor);
	}
	
	private void createVertices(float[] fillColor, float[] outlineColor) {
		/********
		 * ^--^ *
		 * |1/| *
		 * |/2| *
		 * ^--^ *
		 ********/
		// fill triangle 1
		fillMesh.vertex(x, y, fillColor);
		fillMesh.vertex(x + width, y, fillColor);
		fillMesh.vertex(x, y + height, fillColor);
		// fill triangle 2
		fillMesh.vertex(x, y + height, fillColor);
		fillMesh.vertex(x + width, y, fillColor);
		fillMesh.vertex(x + width, y + height, fillColor);
		
		// outline
		outlineMesh.vertex(x, y, outlineColor);
		outlineMesh.vertex(x + width, y, outlineColor);
		outlineMesh.vertex(x + width, y, outlineColor);
		outlineMesh.vertex(x + width, y + height, outlineColor);
		outlineMesh.vertex(x + width, y + height, outlineColor);
		outlineMesh.vertex(x, y + height, outlineColor);
		outlineMesh.vertex(x, y + height, outlineColor);
		outlineMesh.vertex(x, y, outlineColor);
	}
}
