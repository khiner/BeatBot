package com.kh.beatbot.view.mesh;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.view.BBView;

/**
 * A simple Mesh class that wraps OpenGL ES Vertex Arrays. Just instantiate it
 * with the proper parameters then fill it with the color and vertex method.
 * 
 * @author mzechner, adapted by Karl Hiner
 * 
 */
public abstract class Mesh2D {
	public static final float ¹ = (float) Math.PI;
	
	/** The gl instance **/
	private GL11 gl;
	
	/** vertex position buffer and array **/
	protected float vertices[];
	private int vertexHandle = -1;
	private FloatBuffer vertexBuffer;
	
	/** color buffer and array **/
	protected float color[];
	private int colorHandle = -1;
	private FloatBuffer colorBuffer;

	/** vertex index at which the next vertex gets inserted **/
	private int index = 0;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;

	protected int primitiveType;
	
	/** mesh count **/
	public static int meshes = 0;

	public Mesh2D(int primitiveType, int numVertices) {
		this.gl = (GL11) BBView.gl;
		this.numVertices = numVertices;
		this.primitiveType = primitiveType;
		
		int[] buffer = new int[1];
		
		vertices = new float[numVertices * 2];
		gl.glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];
		vertexBuffer = FloatBuffer.wrap(vertices);
		
		color = new float[4];
		gl.glGenBuffers(1, buffer, 0);
		colorHandle = buffer[0];
		colorBuffer = FloatBuffer.wrap(color);
	}

	/**
	 * updates the direct buffers in case the user
	 */
	protected void update() {
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertices.length * 4,
				vertexBuffer, GL11.GL_DYNAMIC_DRAW);
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, color.length * 4,
				colorBuffer, GL11.GL_DYNAMIC_DRAW);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

		index = 0;
	}

	/**
	 * Renders the mesh as the given type, starting at offset using numVertices
	 * vertices.
	 * 
	 * @param primitiveType
	 *            the type
	 * @param offset
	 *            the offset, in number of vertices
	 * @param numVertices
	 *            the number of vertices to use
	 */
	public void render(int offset, int numVertices) {
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		gl.glColorPointer(4, GL10.GL_FLOAT, numVertices * 4, 0);
		
		gl.glDrawArrays(primitiveType, offset, numVertices);
		
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	}

	/**
	 * Renders the mesh as the given type using as many vertices as have been
	 * defined by calling vertex().
	 * 
	 * @param type
	 *            the type
	 */
	public void render() {
		render(0, numVertices);
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
	public void vertex(float x, float y) {
		int offset = index * 2;
		vertices[offset] = x;
		vertices[offset + 1] = y;
		index++;
	}
	
	/**
	 * Sets the color of the current vertex
	 */
	public void setColor(float[] color) {
		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];
		this.color[3] = color[3];
	}

	public int getMaximumVertices() {
		return vertices.length / 2;
	}

	public void dispose() {
		if (vertexHandle != -1)
			gl.glDeleteBuffers(1, new int[] { vertexHandle }, 0);
		if (colorHandle != -1)
			gl.glDeleteBuffers(1, new int[] { colorHandle }, 0);

		vertices = null;
		vertexBuffer = null;
		color = null;
		colorBuffer = null;
		meshes--;
	}
}