package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.texture.TextureGroup;

public class ShapeGroup {

	protected MeshGroup fillGroup, strokeGroup;
	protected TextureGroup textureGroup, textGroup;

	public ShapeGroup() {
		fillGroup = new MeshGroup(GL10.GL_TRIANGLE_STRIP);
		strokeGroup = new MeshGroup(GL10.GL_LINES);
		textureGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, TextureAtlas.resource.getTextureId());
		textGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, TextureAtlas.font.getTextureId());
	}

	public TextureGroup getTextureGroup() {
		return textureGroup;
	}
	
	public TextureGroup getTextGroup() {
		return textGroup;
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
