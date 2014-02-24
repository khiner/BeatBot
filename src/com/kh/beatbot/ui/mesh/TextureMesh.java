package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.texture.TextureGroup;

public class TextureMesh extends Mesh {
	protected int resourceId;

	public TextureMesh(TextureGroup group, int resourceId) {
		this.resourceId = resourceId;
		this.numVertices = Rectangle.NUM_FILL_VERTICES;
		indices = Rectangle.FILL_INDICES;
		setGroup(group);
	}

	public synchronized void setResource(int resourceId, float x, float y,
			float width, float height, float[] color) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).setResource(this, resourceId, x, y, width,
				height, color);
	}
	
	public synchronized void setResource(int resourceId) {
		((TextureGroup) group).setResource(this, resourceId);
	}
	
	@Override
	public synchronized boolean layout(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).layout(this, resourceId, x, y, width, height);
		return true;
	}
}
