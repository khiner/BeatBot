package com.kh.beatbot.ui.mesh;

public abstract class Mesh {
	protected MeshGroup group;
	public int parentVertexIndex = -1, parentIndexOffset = -1;
	protected int numVertices = 0;
	protected short[] indices;
	protected float x, y, width, height;

	public void setGroup(MeshGroup group) {
		if (this.group == group)
			return;
		if (null != this.group) {
			this.group.remove(this);
		}
		this.group = group;
		group.add(this);
	}

	public void setColor(float[] color) {
		group.setColor(this, color);
	}
	
	public void setColor(int vertexIndex, float[] color) {
		group.setColor(this, vertexIndex, color);
	}

	public void destroy() {
		if (group == null)
			return;
		group.remove(this);
		group = null;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public int getNumIndices() {
		return indices.length;
	}

	public short[] getIndices() {
		return indices;
	}

	public int getParentVertexIndex() {
		return parentVertexIndex;
	}

	private void translate(float x, float y) {
		group.translate(this, x, y);
	}
	
	public boolean setPosition(float x, float y) {
		boolean posChanged = x != this.x || y != this.y;
		if (posChanged) {
			translate(x - this.x, y - this.y);
			this.x = x;
			this.y = y;
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean setDimensions(float width, float height) {
		boolean dimChanged = width != this.width || height != this.height;
		if (width <= 0 || height <= 0 || !dimChanged)
			return false;
		else {
			this.width = width;
			this.height = height;
			return true;
		}
	}

	public synchronized boolean layout(float x, float y, float width, float height) {
		boolean dimChanged = setDimensions(width, height);
		boolean posChanged = setPosition(x, y);
		return dimChanged || posChanged;
	}
}
