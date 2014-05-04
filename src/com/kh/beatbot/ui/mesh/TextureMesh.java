package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.texture.TextureGroup;

public class TextureMesh extends Mesh {

	public TextureMesh(TextureGroup group) {
		numVertices = Rectangle.NUM_FILL_VERTICES;
		indices = Rectangle.FILL_INDICES;
		setGroup(group);
	}

	public synchronized void setResource(int resourceId, float x, float y, float width,
			float height, float[] color) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).setResource(this, resourceId, x, y, width, height, color);
	}

	public synchronized void setResource(int resourceId) {
		if (-1 == resourceId) {
			hide();
		} else {
			((TextureGroup) group).setResource(this, resourceId);
		}
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).layout(this, x, y, width, height);
	}
}
