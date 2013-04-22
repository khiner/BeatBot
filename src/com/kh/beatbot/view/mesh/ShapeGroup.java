package com.kh.beatbot.view.mesh;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class ShapeGroup {

	private MeshGroup fillGroup, outlineGroup;

	public ShapeGroup() {
		fillGroup = new MeshGroup();
		outlineGroup = new MeshGroup();
	}
	
	public void draw(GL11 gl, int borderWidth) {
		fillGroup.draw(GL10.GL_TRIANGLES);
		gl.glLineWidth(borderWidth);
		outlineGroup.draw(GL10.GL_LINES);
	}
	
	public void add(Shape shape) {
		fillGroup.addMesh(shape.fillMesh);
		outlineGroup.addMesh(shape.outlineMesh);
	}
	
	public void remove(Shape shape) {
		fillGroup.removeMesh(shape.fillMesh);
		outlineGroup.removeMesh(shape.outlineMesh);
	}
	
	public void replace(Shape oldShape, Shape newShape) {
		fillGroup.replaceMesh(oldShape.fillMesh, newShape.fillMesh);
		outlineGroup.replaceMesh(oldShape.outlineMesh, newShape.outlineMesh);
	}
	
	public void update(Shape shape) {
		fillGroup.updateVertices(shape.fillMesh);
		outlineGroup.updateVertices(shape.outlineMesh);
	}
	
	public void clear() {
		fillGroup.clear();
		outlineGroup.clear();
	}
}
