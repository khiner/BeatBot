package com.kh.beatbot.ui.mesh;

public class Mesh2D {

	protected MeshGroup group;
	/** vertex index at which the next vertex gets inserted (and parent) **/
	protected int index = 0, parentVertexIndex = -1;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;

	public Mesh2D(MeshGroup group, int numVertices) {
		this.numVertices = numVertices;
		setGroup(group);
	}

	public void setGroup(MeshGroup group) {
		if (this.group == group)
			return;
		this.group = group;
		group.add(this);
	}

	public void vertex(float x, float y, float[] color) {
		group.vertex(this, x, y, color);
		index++;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public void translate(float x, float y) {
		group.translate(this, x, y);
	}
}