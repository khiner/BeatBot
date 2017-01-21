package com.odang.beatbot.ui.mesh;

import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.texture.TextureGroup;

public class TextureMesh extends Mesh {
	private int resourceId = -1;

	public TextureMesh(TextureGroup group) {
		numVertices = Rectangle.NUM_FILL_VERTICES;
		this.group = group;
	}

	public void setResource(int resourceId) {
		if (resourceId == this.resourceId)
			return;

		this.resourceId = resourceId;
		if (resourceId == -1) {
			hide();
		} else {
			show();
		}
	}

	public void show() {
		if (resourceId != -1) {
			super.show();
			((TextureGroup) group).setResource(this, resourceId);
		}
	}

	@Override
	public void layout(float x, float y, float width, float height) {
		if (!isVisible())
			return;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		((TextureGroup) group).layout(this, x, y, width, height);
	}
	
	@Override
	public int getNumIndices() {
		return Rectangle.FILL_INDICES.length; 
	}

	@Override
	public short getIndex(int i) {
		return Rectangle.FILL_INDICES[i];
	}
}
