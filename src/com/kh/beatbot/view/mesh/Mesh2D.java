package com.kh.beatbot.view.mesh;


/**
 * A simple Mesh class that wraps OpenGL ES Vertex Arrays. Just instantiate it
 * with the proper parameters then fill it with the color and vertex method.
 * 
 * @author mzechner, adapted by Karl Hiner
 * 
 */
public abstract class Mesh2D {
	public static final float ¹ = (float) Math.PI;
	
	public MeshGroup parent;
	
	public int parentVertexIndex = -1;
	protected float vertices[];
	protected float color[];
	
	/** vertex index at which the next vertex gets inserted **/
	protected int index = 0;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;

	public Mesh2D(int numVertices) {
		this.numVertices = numVertices;
		
		vertices = new float[numVertices * 2];
		color = new float[numVertices * 4];
	}

	public Mesh2D(float[] vertices, float[] color) {
		this.numVertices = vertices.length / 2;
		this.vertices = vertices;
		this.color = new float[numVertices * 4];
		setColor(color);
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
		this.color[colorOffset] = color[0];
		this.color[colorOffset + 1] = color[1];
		this.color[colorOffset + 2] = color[2];
		this.color[colorOffset + 3] = color[3];
		index++;
	}

	public int getNumVertices() {
		return numVertices;
	}
	
	public float[] getVertices() {
		return vertices;
	}
	
	public float[] getColor() {
		return color;
	}
	
	/** set all vertices to this color **/
	public void setColor(float[] color) {
		for (int i = 0; i < numVertices; i++) {
			this.color[i * 4] = color[0];
			this.color[i * 4 + 1] = color[1];
			this.color[i * 4 + 2] = color[2];
			this.color[i * 4 + 3] = color[3];
		}
	}
}