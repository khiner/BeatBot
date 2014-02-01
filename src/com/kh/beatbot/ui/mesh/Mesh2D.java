package com.kh.beatbot.ui.mesh;

public class Mesh2D extends Mesh {

	/** vertex index at which the next vertex gets inserted (and parent) **/
	protected int index = 0;

	public Mesh2D(Mesh2DGroup group, int numVertices) {
		this.numVertices = numVertices;
		setGroup(group);
	}

	public void vertex(float x, float y, float[] color) {
		if (index >= numVertices) {
			numVertices = index + 1;
			group.changeSize(this, numVertices - 1, numVertices);
		}
		((Mesh2DGroup)group).vertex(this, x, y, color);
		index++;
	}
}