package com.kh.beatbot.ui.shape;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.mesh.MeshGroup;
import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.texture.TextureGroup;

public class RenderGroup {
	private MeshGroup fillGroup, strokeGroup;
	private TextureGroup textureGroup, textGroup;

	public RenderGroup() {
		fillGroup = new MeshGroup(GL10.GL_TRIANGLE_STRIP);
		strokeGroup = new MeshGroup(GL10.GL_LINES);
		textureGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP,
				TextureAtlas.resource.getTextureId());
		textGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, TextureAtlas.font.getTextureId());
	}

	public TextureGroup getTextureGroup() {
		return textureGroup;
	}

	public TextureGroup getTextGroup() {
		return textGroup;
	}

	public MeshGroup getFillGroup() {
		return fillGroup;
	}

	public MeshGroup getStrokeGroup() {
		return strokeGroup;
	}

	public void draw() {
		fillGroup.draw();
		strokeGroup.draw();
		textureGroup.draw();
		textGroup.draw();
	}

	public void translate(float x, float y) {
		fillGroup.translate(x, y);
		strokeGroup.translate(x, y);
		textureGroup.translate(x, y);
		textGroup.translate(x, y);
	}
}
