package com.kh.beatbot.ui.mesh;

public abstract class Mesh {
	protected MeshGroup group;
	protected int numVertices = 0;
	protected short[] indices;
	public float x, y, width, height;

	private int groupVertexOffset = -1, groupIndexOffset = -1;

	public boolean containsPoint(float x, float y) {
		return this.x < x && this.x + width > x && this.y < y && this.y + height > y;
	}

	public MeshGroup getGroup() {
		return group;
	}

	public void setColor(float[] color) {
		if (null == color) {
			hide();
		} else if (isVisible()) {
			group.setColor(this, color);
		}
	}

	public void setColor(int vertexIndex, float[] color) {
		if (isVisible()) {
			group.setColor(this, vertexIndex, color);
		}
	}

	public void hide() {
		if (isVisible()) {
			group.remove(this);
		}
	}

	public void show() {
		if (!isVisible()) {
			group.add(this);
		}
	}

	public boolean isVisible() {
		return null != group && group.contains(this) && getNumVertices() > 0;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public int getNumIndices() {
		return null == indices ? getNumVertices() : indices.length;
	}

	public short getIndex(int i) {
		return null == indices ? (short) i : indices[i];
	}

	public int getGroupVertexOffset() {
		return groupVertexOffset;
	}
	
	public int getGroupIndexOffset() {
		return groupIndexOffset;
	}
	
	public void setGroupVertexOffset(int groupVertexOffset) {
		this.groupVertexOffset = groupVertexOffset;
	}
	
	public void setGroupIndexOffset(int groupIndexOffset) {
		this.groupIndexOffset = groupIndexOffset;
	}

	private void translate(float x, float y) {
		if (isVisible()) {
			group.translate(this, x, y);
		}
	}

	public void setPosition(float x, float y) {
		translate(x - this.x, y - this.y);
		this.x = x;
		this.y = y;
	}

	public synchronized void setDimensions(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public synchronized void layout(float x, float y, float width, float height) {
		setDimensions(width, height);
		setPosition(x, y);
	}
}
