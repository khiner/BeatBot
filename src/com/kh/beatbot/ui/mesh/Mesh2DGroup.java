package com.kh.beatbot.ui.mesh;

import java.nio.ShortBuffer;

public class Mesh2DGroup extends MeshGroup {

	public Mesh2DGroup(int primitiveType) {
		super(primitiveType, 6, false);
	}

	protected synchronized void vertex(Mesh2D mesh, float x, float y,
			float[] color) {
		int vertex = (mesh.index + mesh.parentVertexIndex) * indicesPerVertex;

		vertices[vertex] = x;
		vertices[vertex + 1] = y;
		vertices[vertex + 2] = color[0];
		vertices[vertex + 3] = color[1];
		vertices[vertex + 4] = color[2];
		vertices[vertex + 5] = color[3];

		dirty = true;
	}

	protected synchronized void updateIndices() {
		short[] indices = new short[numVertices];
		for (short i = 0; i < indices.length; i++) {
			indices[i] = i;
		}

		indexBuffer = ShortBuffer.wrap(indices);
	}
}
