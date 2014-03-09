package com.kh.beatbot.ui.mesh;

import android.util.Log;

public class Mesh2D extends Mesh {

	/** vertex index at which the next vertex gets inserted (and parent) **/
	protected int index = 0;
	boolean customIndices = false;

	public Mesh2D(MeshGroup group, int numVertices) {
		setNumVertices(numVertices);
		setGroup(group);
	}

	public Mesh2D(MeshGroup group, int numVertices, short[] indices) {
		if (null != indices) {
			customIndices = true;
			this.indices = indices;
			this.numVertices = numVertices;
		} else {
			setNumVertices(numVertices);
		}
		setGroup(group);
	}

	public void vertex(float x, float y, float[] color) {
		if (index >= numVertices) {
			if (customIndices) {
				Log.e("Mesh2D", "trying to expand custom indices");
			}
			setNumVertices(index + 1);
			group.changeSize(this, numVertices - 1, numVertices, numVertices - 1, numVertices);
		}
		group.vertex(this, index, x, y, color);
		index++;
	}

	public void resetIndex() {
		index = 0;
	}

	public boolean isFull() {
		return index >= numVertices;
	}

	private void setNumVertices(int numVertices) {
		this.numVertices = numVertices;
		this.indices = new short[numVertices];
		for (short i = 0; i < this.indices.length; i++) {
			this.indices[i] = i;
		}
	}
}