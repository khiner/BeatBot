package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.view.View;

public class ShapeGroup {

	private MeshGroup fillGroup, outlineGroup;
	private int strokeWeight = 0;

	public ShapeGroup() {
		fillGroup = new MeshGroup(GL10.GL_TRIANGLES);
		outlineGroup = new MeshGroup(GL10.GL_LINES);
	}

	public void setStrokeWeight(final int strokeWeight) {
		this.strokeWeight = strokeWeight;
	}

	public void setFillPrimitiveType(final int primitiveType) {
		fillGroup.setPrimitiveType(primitiveType);
	}

	public void setOutlinePrimitiveType(int primitiveType) {
		outlineGroup.setPrimitiveType(primitiveType);
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
		outlineGroup.draw();
	}

	public boolean contains(Shape shape) {
		if (shape == null) {
			return false;
		}
		return fillGroup.contains(shape.getFillMesh())
				|| outlineGroup.contains(shape.getStrokeMesh());
	}

	public void add(Shape shape) {
		if (shape != null) {
			fillGroup.add(shape.getFillMesh());
			outlineGroup.add(shape.getStrokeMesh());
		}
	}

	public void remove(Shape shape) {
		if (shape != null) {
			fillGroup.remove(shape.getFillMesh());
			outlineGroup.remove(shape.getStrokeMesh());
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
		outlineGroup
				.replace(oldShape.getStrokeMesh(), newShape.getStrokeMesh());
	}

	public void update(Shape shape) {
		fillGroup.updateVertices(shape.getFillMesh());
		outlineGroup.updateVertices(shape.getStrokeMesh());
	}

	public void clear() {
		fillGroup.clear();
		outlineGroup.clear();
	}
}
