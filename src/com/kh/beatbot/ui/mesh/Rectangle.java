package com.kh.beatbot.ui.mesh;


public class Rectangle extends Shape {
	
	public Rectangle(ShapeGroup group, float[] fillColor) {
		// 6 vertices for rect fill (two triangles)
		super(group, new Mesh2D(6, fillColor));
	}
	
	public Rectangle(ShapeGroup group, float[] fillColor, float[] outlineColor) {
		// 6 vertices for rect fill (two triangles)
		// 8 vertices for rect outline (4 sides * 2 vertices per side)
		super(group, new Mesh2D(6, fillColor), new Mesh2D(8, outlineColor));
	}


	public void update(float x, float y, float width, float height, float[] fillColor) {
		update(x, y, width, height, fillColor, null);
	}
	
	public void update(float x, float y, float width, float height, float[] fillColor, float[] outlineColor) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		fillMesh.color = fillColor;
		if (outlineColor != null) {
			outlineMesh.color = outlineColor;
		}
		update();
	}
	
	/********
	 * ^--^ *
	 * |1/| *
	 * |/2| *
	 * ^--^ *
	 ********/
	protected void createVertices(float[] fillColor) {
		// fill triangle 1
		fillMesh.vertex(x, y);
		fillMesh.vertex(x + width, y);
		fillMesh.vertex(x, y + height);
		// fill triangle 2
		fillMesh.vertex(x, y + height);
		fillMesh.vertex(x + width, y);
		fillMesh.vertex(x + width, y + height);
		
		fillMesh.setColor(fillColor);	
	}
	
	protected void createVertices(float[] fillColor, float[] outlineColor) {
		createVertices(fillColor);
		// outline
		outlineMesh.vertex(x, y);
		outlineMesh.vertex(x + width, y);
		outlineMesh.vertex(x + width, y);
		outlineMesh.vertex(x + width, y + height);
		outlineMesh.vertex(x + width, y + height);
		outlineMesh.vertex(x, y + height);
		outlineMesh.vertex(x, y + height);
		outlineMesh.vertex(x, y);
		
		outlineMesh.setColor(outlineColor);
	}
}
