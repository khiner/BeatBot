package com.kh.beatbot.view.mesh;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * A simple Mesh class that wraps OpenGL ES Vertex Arrays. Just instantiate it
 * with the proper parameters then fill it with the color and vertex method.
 * 
 * @author mzechner, adapted by Karl Hiner
 * 
 */
public class Mesh2D {
	public static final float ¹ = (float) Math.PI;
	
	/** The gl instance **/
	private GL10 gl;

	protected float outlineVertices[];
	
	/** vertex position buffer and array **/
	protected float vertices[];
	private int vertexHandle = -1;
	private FloatBuffer vertexBuffer;

	/** color buffer and array **/
	protected float colors[];
	private int colorHandle = -1;
	private FloatBuffer colorBuffer;

	/** vertex index at which the next vertex gets inserted **/
	private int index = 0, outlineIndex = 0;

	/** number of vertices defined for the mesh **/
	protected int numVertices = 0;

	/** is the mesh dirty? **/
	private boolean dirty = true;

	/** last mesh **/
	private static Mesh2D lastMesh;

	/** mesh count **/
	public static int meshes = 0;

	public Mesh2D(GL10 gl, int numVertices, int numOutlineVertices, boolean hasColors) {
		this.gl = gl;
		this.numVertices = numVertices;
		vertices = new float[numVertices * 2];
		outlineVertices = new float[numOutlineVertices * 2];
		int[] buffer = new int[1];

		((GL11) gl).glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];
		vertexBuffer = FloatBuffer.wrap(vertices);

		if (hasColors) {
			colors = new float[4];
			((GL11) gl).glGenBuffers(1, buffer, 0);
			colorHandle = buffer[0];
			colorBuffer = FloatBuffer.wrap(colors);
		}
	}

	/**
	 * updates the direct buffers in case the user
	 */
	private void update() {
		GL11 gl = (GL11) this.gl;

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertices.length * 4,
				vertexBuffer, GL11.GL_DYNAMIC_DRAW);

		if (colors != null) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
			gl.glBufferData(GL11.GL_ARRAY_BUFFER, colors.length * 4,
					colorBuffer, GL11.GL_DYNAMIC_DRAW);
		}

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

		index = 0;
		dirty = false;
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
	public void render(int primitiveType, int offset, int numVertices) {
		boolean wasDirty = dirty;
		if (dirty)
			update();

		if (this == lastMesh && !wasDirty) {
			gl.glDrawArrays(primitiveType, offset, numVertices);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			return;
		}

		((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		((GL11) gl).glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

		if (colors != null) {
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
			((GL11) gl).glColorPointer(4, GL10.GL_FLOAT, numVertices * 4, 0);
		}

		gl.glDrawArrays(primitiveType, offset, numVertices);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		lastMesh = this;
	}

	/**
	 * Renders the mesh as the given type using as many vertices as have been
	 * defined by calling vertex().
	 * 
	 * @param type
	 *            the type
	 */
	public void render(int primitiveType) {
		render(primitiveType, 0, numVertices);
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
		dirty = true;
		int offset = index * 2;
		vertices[offset] = x;
		vertices[offset + 1] = y;
		index++;
	}

	public void outlineVertex(float x, float y) {
		dirty = true;
		int offset = outlineIndex * 2;
		outlineVertices[offset] = x;
		outlineVertices[offset + 1] = y;
		outlineIndex++;
	}
	
	/**
	 * Sets the color of the current vertex
	 * 
	 * @param r
	 *            the red component
	 * @param g
	 *            the green component
	 * @param b
	 *            the blue component
	 * @param a
	 *            the alpha component
	 */
	public void color(float[] colors) {
		dirty = true;
		this.colors[0] = colors[0];
		this.colors[1] = colors[1];
		this.colors[2] = colors[2];
		this.colors[3] = colors[3];
	}

	public int getMaximumVertices() {
		return vertices.length / 2;
	}

	public void dispose() {
		GL11 gl = (GL11) this.gl;
		if (vertexHandle != -1)
			gl.glDeleteBuffers(1, new int[] { vertexHandle }, 0);
		if (colorHandle != -1)
			gl.glDeleteBuffers(1, new int[] { colorHandle }, 0);

		vertices = null;
		vertexBuffer = null;
		colors = null;
		colorBuffer = null;
		meshes--;
	}
}