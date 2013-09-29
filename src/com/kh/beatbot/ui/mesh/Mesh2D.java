package com.kh.beatbot.ui.mesh;


/**
 * A simple Mesh class that wraps OpenGL ES Vertex Arrays. Just instantiate it
 * with the proper parameters then fill it with the color and vertex method.
 * 
 * @author mzechner, adapted by Karl Hiner
 * 
 */
public class Mesh2D {

	protected float vertices[], colors[], color[];
	
	/** vertex index at which the next vertex gets inserted (and parent)**/
	protected int index = 0, parentVertexIndex = -1;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;
	
	public Mesh2D(int numVertices, float[] color) {
		this.numVertices = numVertices;
		this.color = color;
		vertices = new float[numVertices * 2];
		colors = new float[numVertices * 4];
	}

	public Mesh2D(float[] vertices, float[] color) {
		this.numVertices = vertices.length / 2;
		this.vertices = vertices;
		this.colors = new float[numVertices * 4];
		this.color = new float[4];
		setColor(color);
	}
	
	public void vertex(float x, float y) {
		int vertexOffset = index * 2;
		vertices[vertexOffset] = x;
		vertices[vertexOffset + 1] = y;
		index++;
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

	/** set all vertices to this color **/
	public void setColor(float[] color) {
		this.color = color;
		for (int i = 0; i < numVertices; i++) {
			setColor(i, color);
		}
	}
	
	public void setColor(int index, float[] color) {
		this.colors[index * 4] = color[0];
		this.colors[index * 4 + 1] = color[1];
		this.colors[index * 4 + 2] = color[2];
		this.colors[index * 4 + 3] = color[3];
	}
}