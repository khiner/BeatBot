package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.texture.TextureGroup;

public class TextureMesh extends Mesh {
	private int resourceId = -1;

	public TextureMesh(TextureGroup group) {
		numVertices = Rectangle.NUM_FILL_VERTICES;
		this.group = group;
		show();
	}

	public synchronized void setResource(int resourceId) {
		this.resourceId = resourceId;
		if (resourceId == -1) {
			hide();
		} else {
			show();
			((TextureGroup) group).setResource(this, resourceId);
		}
	}

	public void show() {
		if (resourceId != -1) {
			super.show();
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
	
	@Override
	public int getNumIndices() {
		return Rectangle.FILL_INDICES.length; 
	}

	@Override
	public short getIndex(int i) {
		return Rectangle.FILL_INDICES[i];
	}
}
