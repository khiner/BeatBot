package com.kh.beatbot.ui.mesh;

public abstract class Mesh {
	protected MeshGroup group;
	protected int parentVertexIndex = -1, parentIndexOffset = -1;
	protected int numVertices = 0;
	protected short[] indices;

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

	public void translate(float x, float y) {
		group.translate(this, x, y);
	}
}
