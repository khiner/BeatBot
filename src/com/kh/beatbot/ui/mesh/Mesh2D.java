package com.kh.beatbot.ui.mesh;

/**
 * A simple Mesh class that wraps OpenGL ES Vertex Buffer Arrays
 */
public class Mesh2D {

	protected MeshGroup group;

	protected float vertices[], colors[];
	private float color[];

	/** vertex index at which the next vertex gets inserted (and parent) **/
	protected int index = 0, parentVertexIndex = -1;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;

	public Mesh2D(MeshGroup group, int numVertices, float[] color) {
		this.group = group;
		this.numVertices = numVertices;
		vertices = new float[numVertices * 2];
		colors = new float[numVertices * 4];
		this.color = color;
	}

	/**
	 * Defines the position of the current vertex. Before you call this you have
	 * to call any other method like color for the current vertex!
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public void vertex(float x, float y, float[] color) {
		int vertexOffset = index * 2;
		int colorOffset = index * 4;
		vertices[vertexOffset] = x;
		vertices[vertexOffset + 1] = y;
		colors[colorOffset] = color[0];
		colors[colorOffset + 1] = color[1];
		colors[colorOffset + 2] = color[2];
		colors[colorOffset + 3] = color[3];
		index++;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public float[] getVertices() {
		return vertices;
	}

	public void setColor(float[] color) {
		this.color = color;
	}

	public float[] getColor() {
		return color;
	}

	public void translate(float x, float y) {
		for (int i = 0; i < numVertices; i++) {
			vertices[i * 2] += x;
			vertices[i * 2 + 1] += y;
		}
	}
}