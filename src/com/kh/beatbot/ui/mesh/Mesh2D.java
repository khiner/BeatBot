package com.kh.beatbot.ui.mesh;

/**
 * A simple Mesh class that wraps OpenGL ES Vertex Buffer Arrays
 */
public class Mesh2D {

	protected MeshGroup group;
	private float color[];

	/** vertex index at which the next vertex gets inserted (and parent) **/
	protected int index = 0, parentVertexIndex = -1;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;

	public Mesh2D(MeshGroup group, int numVertices, float[] color) {
		this.numVertices = numVertices;
		this.color = color;
		setGroup(group);
	}

	public void setGroup(MeshGroup group) {
		this.group = group;
		group.add(this);
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
		group.vertex(parentVertexIndex + index, x, y, color);
		index++;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public void setColor(float[] color) {
		this.color = color;
	}

	public float[] getColor() {
		return color;
	}

	public void translate(float x, float y) {
		for (int i = 0; i < numVertices; i++) {
			group.translate(i + parentVertexIndex, x, y);
		}
	}
}