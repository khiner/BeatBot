package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.texture.TextureGroup;
import com.kh.beatbot.ui.view.View;

public class ShapeGroup {

	protected Mesh2DGroup fillGroup, strokeGroup;
	protected TextureGroup textureGroup, textGroup;

	public ShapeGroup() {
		fillGroup = new Mesh2DGroup(GL10.GL_TRIANGLE_STRIP);
		strokeGroup = new Mesh2DGroup(GL10.GL_LINES);
		textureGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, TextureAtlas.resource.getTextureId());
		textGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, TextureAtlas.font.getTextureId());
	}

	public TextureGroup getTextureGroup() {
		return textureGroup;
	}
	
	public TextureGroup getTextGroup() {
		return textGroup;
	}

	public void draw(View parent) {
		View.push();
		View.translate(-parent.absoluteX, -parent.absoluteY);
		draw();
		View.pop();
	}

	public void draw() {
		fillGroup.draw();
		strokeGroup.draw();
		textureGroup.draw();
		textGroup.draw();
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
