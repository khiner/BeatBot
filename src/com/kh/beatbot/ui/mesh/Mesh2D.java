package com.kh.beatbot.ui.mesh;

public class Mesh2D extends Mesh {
	/** vertex index at which the next vertex gets inserted (and parent) **/
	private int index = 0;

	public Mesh2D(MeshGroup group, int numVertices, short[] indices) {
		this.indices = indices;
		this.numVertices = numVertices;
		this.group = group;
		show();
	}

	public void vertex(float x, float y, float[] color) {
		if (!isVisible())
			return;
		if (isFull()) {
			numVertices = index + 1;
			group.changeSize(this, numVertices - 1, numVertices, numVertices - 1, numVertices);
		}
		group.vertex(this, index, x, y, color);
		index++;
	}

	public void push() {
		group.push(this);
	}

	public void setStrokeWeight(int weight) {
		group.setStrokeWeight(weight);
	}

	public void reset() {
		index = 0;
	}

	public boolean isFull() {
		return index >= numVertices;
	}
}