package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.view.View;

public class ShapeGroup {

	protected MeshGroup fillGroup, strokeGroup;
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
		if (shape.getFillMesh() != null) {
			shape.getFillMesh().setGroup(fillGroup);
		}
		if (shape.getStrokeMesh() != null) {
			shape.getStrokeMesh().setGroup(strokeGroup);
		}
		shape.update();
	}

	public void remove(Shape shape) {
		if (shape != null) {
			fillGroup.remove(shape.getFillMesh());
			strokeGroup.remove(shape.getStrokeMesh());
		}
	}

	// put at the top of the stack, so it is displayed ontop of all others
	public void push(Shape shape) {
		fillGroup.push(shape.fillMesh);
		strokeGroup.push(shape.strokeMesh);
	}
}
