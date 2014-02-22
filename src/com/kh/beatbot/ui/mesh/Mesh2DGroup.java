package com.kh.beatbot.ui.mesh;


public class Mesh2DGroup extends MeshGroup {

	public Mesh2DGroup(int primitiveType) {
		super(primitiveType, 6);
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
}
