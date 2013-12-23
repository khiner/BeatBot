package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.view.View;

public class ShapeGroup {

	public MeshGroup fillGroup, strokeGroup;
	private int strokeWeight = 0;

	public ShapeGroup() {
		fillGroup = new MeshGroup(GL10.GL_TRIANGLES);
		strokeGroup = new MeshGroup(GL10.GL_LINES);
	}

	public void setStrokeWeight(final int strokeWeight) {
		this.strokeWeight = strokeWeight;
	}

	public void setFillPrimitiveType(final int primitiveType) {
		fillGroup.setPrimitiveType(primitiveType);
	}

	public void setStrokePrimitiveType(int primitiveType) {
		strokeGroup.setPrimitiveType(primitiveType);
	}

	public void draw(View parent, int borderWeight) {
		View.push();
		View.translate(-parent.absoluteX, -parent.absoluteY);
		draw();
		View.pop();
	}

	public void draw() {
		fillGroup.draw();
		View.gl.glLineWidth(strokeWeight);
		strokeGroup.draw();
	}

	public boolean contains(Shape shape) {
		if (shape == null) {
			return false;
		}
		return fillGroup.contains(shape.getFillMesh())
				|| strokeGroup.contains(shape.getStrokeMesh());
	}

	public void add(Shape shape) {
		if (shape == null)
			return;
		fillGroup.add(shape.getFillMesh());
		strokeGroup.add(shape.getStrokeMesh());
		fillGroup.updateVertices(shape.getFillMesh());
		strokeGroup.updateVertices(shape.getStrokeMesh());
	}

	public void remove(Shape shape) {
		if (shape != null) {
			fillGroup.remove(shape.getFillMesh());
			strokeGroup.remove(shape.getStrokeMesh());
		}
	}

	public void replace(Shape oldShape, Shape newShape) {
		if (newShape == null) {
			remove(oldShape);
			return;
		}
		if (oldShape == null) {
			add(newShape);
			return;
		}
		fillGroup.replace(oldShape.getFillMesh(), newShape.getFillMesh());
		strokeGroup.replace(oldShape.getStrokeMesh(), newShape.getStrokeMesh());

		if (oldShape.getFillMesh() == null || newShape.getFillMesh() != null) {
			fillGroup.updateVertices(newShape.getFillMesh());
		}

		if (oldShape.getStrokeMesh() == null
				|| newShape.getStrokeMesh() != null) {
			strokeGroup.updateVertices(newShape.getStrokeMesh());
		}
	}

	public void update(Shape shape) {
		fillGroup.updateVertices(shape.getFillMesh());
		strokeGroup.updateVertices(shape.getStrokeMesh());
	}
}
