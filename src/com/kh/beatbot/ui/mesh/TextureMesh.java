package com.kh.beatbot.ui.mesh;

public class TextureMesh extends Mesh {
	protected int resourceId;
	protected float x, y, width, height;

	public TextureMesh(TextureGroup group, int resourceId) {
		this.resourceId = resourceId;
		this.numVertices = Rectangle.NUM_FILL_VERTICES;
		indices = Rectangle.FILL_INDICES;
		setGroup(group);
	}

	public synchronized void setTexture(int resourceId, float x, float y,
			float width, float height, float[] color) {
		if (resourceId == this.resourceId && width == this.width
				&& height == this.height && (y != this.y || x != this.x)) {
			translate(x - this.x, y - this.y);
			this.x = x;
			this.y = y;
			return;
		}

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).setTexture(this, resourceId, x, y, width,
				height, color);
	}
}
