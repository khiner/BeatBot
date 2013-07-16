package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.view.View;

public class ShapeGroup {

	private MeshGroup fillGroup, outlineGroup;
	
	public ShapeGroup() {
		fillGroup = new MeshGroup();
		outlineGroup = new MeshGroup();
	}

	public void draw(View parent, int borderWidth) {
		View.push();
		View.translate(-parent.absoluteX, -parent.absoluteY);
		draw(borderWidth);
		View.pop();
	}
	
	public void draw(int borderWidth) {
		fillGroup.draw(GL10.GL_TRIANGLES);
		if (borderWidth > 0) {
			View.gl.glLineWidth(borderWidth);
			outlineGroup.draw(GL10.GL_LINES);
		}
	}

	public boolean contains(Shape shape) {
		return fillGroup.contains(shape.fillMesh) && (shape.outlineMesh == null
				|| outlineGroup.contains(shape.outlineMesh));
	}

	public void add(Shape shape) {
		fillGroup.add(shape.fillMesh);
		if (shape.outlineMesh != null) {
			outlineGroup.add(shape.outlineMesh);
		}
	}

	public void remove(Shape shape) {
		fillGroup.remove(shape.fillMesh);
		outlineGroup.remove(shape.outlineMesh);
	}

	public void replace(Shape oldShape, Shape newShape) {
		fillGroup.replace(oldShape.fillMesh, newShape.fillMesh);
		if (oldShape.outlineMesh != null && newShape.outlineMesh != null) {
			outlineGroup.replace(oldShape.outlineMesh, newShape.outlineMesh);
		}
	}

	public void update(Shape shape) {
		fillGroup.updateVertices(shape.fillMesh);
		if (shape.outlineMesh != null) {
			outlineGroup.updateVertices(shape.outlineMesh);
		}
	}

	public void clear() {
		fillGroup.clear();
		outlineGroup.clear();
	}
}
