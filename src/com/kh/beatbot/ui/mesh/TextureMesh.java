package com.kh.beatbot.ui.mesh;

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
		if (resourceId == this.resourceId && width == this.width
				&& height == this.height) {
			setPosition(x, y);
			return;
		}

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).setResource(this, resourceId, x, y, width,
				height, color);
	}
}
