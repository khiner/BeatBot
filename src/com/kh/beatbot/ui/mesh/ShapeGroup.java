package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.view.View;

public class ShapeGroup {

	private MeshGroup fillGroup, outlineGroup;

	public ShapeGroup() {
		fillGroup = new MeshGroup(GL10.GL_TRIANGLES);
		outlineGroup = new MeshGroup(GL10.GL_LINES);
	}

	public void setFillPrimitiveType(int primitiveType) {
		fillGroup.setPrimitiveType(primitiveType);
	}

	public void setOutlinePrimitiveType(int primitiveType) {
		outlineGroup.setPrimitiveType(primitiveType);
	}

	public void draw(View parent, int borderWeight) {
		View.push();
		View.translate(-parent.absoluteX, -parent.absoluteY);
		draw(borderWeight);
		View.pop();
	}

	public void draw(int borderWeight) {
		fillGroup.draw();
		if (borderWeight > 0) {
			View.gl.glLineWidth(borderWeight);
			outlineGroup.draw();
		}
	}

	public boolean contains(Shape shape) {
		return fillGroup.contains(shape.fillMesh)
				&& (shape.strokeMesh == null || outlineGroup
						.contains(shape.strokeMesh));
	}

	public void add(Shape shape) {
		fillGroup.add(shape.fillMesh);
		if (shape.strokeMesh != null) {
			outlineGroup.add(shape.strokeMesh);
		}
	}

	public void remove(Shape shape) {
		fillGroup.remove(shape.fillMesh);
		outlineGroup.remove(shape.strokeMesh);
	}

	public void replace(Shape oldShape, Shape newShape) {
		fillGroup.replace(oldShape.fillMesh, newShape.fillMesh);
		if (oldShape.strokeMesh != null && newShape.strokeMesh != null) {
			outlineGroup.replace(oldShape.strokeMesh, newShape.strokeMesh);
		}
	}

	public void update(Shape shape) {
		fillGroup.updateVertices(shape.fillMesh);
		if (shape.strokeMesh != null) {
			outlineGroup.updateVertices(shape.strokeMesh);
		}
	}

	public void clear() {
		fillGroup.clear();
		outlineGroup.clear();
	}
}
