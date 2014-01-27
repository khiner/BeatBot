package com.kh.beatbot.ui.mesh;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.util.Log;

import com.kh.beatbot.ui.view.View;

public class MeshGroup {
	private static final int INDICES_PER_VERTEX = 6; // x, y + color
	private static final int FLOAT_BYTES = Float.SIZE / 8;
	private static final int COLOR_OFFSET_BYTES = FLOAT_BYTES * 2;
	private static final int VERTEX_BYTES = INDICES_PER_VERTEX * FLOAT_BYTES;

	private List<Mesh2D> children = new ArrayList<Mesh2D>();
	private FloatBuffer vertexBuffer;
	private float[] vertices = new float[0];
	private int vertexHandle = -1, numVertices = 0, primitiveType;
	private boolean dirty = false;

	public MeshGroup(int primitiveType) {
		this.primitiveType = primitiveType;
	}

	protected float getVertexX(int index) {
		return vertices[index * INDICES_PER_VERTEX];
	}

	protected float getVertexY(int index) {
		return vertices[index * INDICES_PER_VERTEX + 1];
	}

	protected synchronized void vertex(Mesh2D mesh, float x, float y,
			float[] color) {
		int vertex = (mesh.index + mesh.parentVertexIndex) * INDICES_PER_VERTEX;

		vertices[vertex] = x;
		vertices[vertex + 1] = y;
		vertices[vertex + 2] = color[0];
		vertices[vertex + 3] = color[1];
		vertices[vertex + 4] = color[2];
		vertices[vertex + 5] = color[3];

		dirty = true;
	}

	protected synchronized void translate(Mesh2D mesh, float x, float y) {
		for (int i = mesh.parentVertexIndex * INDICES_PER_VERTEX; i < (mesh.parentVertexIndex + mesh.numVertices)
				* INDICES_PER_VERTEX; i += INDICES_PER_VERTEX) {
			vertices[i] += x;
			vertices[i + 1] += y;
		}
		dirty = true;
	}

	public void setPrimitiveType(int primitiveType) {
		this.primitiveType = primitiveType;
	}

	public synchronized void draw() {
		if (children.isEmpty()) {
			return;
		}

		if (dirty) {
			updateBuffers();
			dirty = false;
		}

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		View.gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		View.gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_BYTES, 0);
		View.gl.glColorPointer(4, GL10.GL_FLOAT, VERTEX_BYTES, COLOR_OFFSET_BYTES);

		View.gl.glDrawArrays(primitiveType, 0, numVertices);

		View.gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	}

	public synchronized boolean contains(Mesh2D mesh) {
		return children.contains(mesh);
	}

	public synchronized void add(Mesh2D mesh) {
		if (mesh == null || children.contains(mesh)) {
			return;
		}
		mesh.parentVertexIndex = numVertices;
		children.add(mesh);
		numVertices += mesh.getNumVertices();
		vertices = Arrays.copyOf(vertices, numVertices * INDICES_PER_VERTEX);
		vertexBuffer = FloatBuffer.wrap(vertices);
		dirty = true;
	}

	public synchronized void remove(Mesh2D mesh) {
		if (mesh == null) {
			return;
		} else if (!children.contains(mesh)) {
			Log.e("MeshGroup",
					"Attempting to remove a mesh that is not a child.");
			return;
		}

		int src = mesh.parentVertexIndex * INDICES_PER_VERTEX;
		int dst = (mesh.parentVertexIndex + mesh.numVertices)
				* INDICES_PER_VERTEX;
		System.arraycopy(vertices, dst, vertices, src, vertices.length - dst);

		children.remove(mesh);

		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			currVertexIndex += child.getNumVertices();
		}
		numVertices = currVertexIndex;
		vertexBuffer.limit(numVertices * INDICES_PER_VERTEX);

		dirty = true;
	}

	public synchronized void push(Mesh2D mesh) {
		if (mesh == null || !children.contains(mesh))
			return;

		int src = mesh.parentVertexIndex * INDICES_PER_VERTEX;
		int dst = (mesh.parentVertexIndex + mesh.numVertices)
				* INDICES_PER_VERTEX;

		// move vertices to the end of the array
		float[] tmp = Arrays.copyOfRange(vertices, src, dst);
		System.arraycopy(vertices, dst, vertices, src, vertexBuffer.limit()
				- dst);
		System.arraycopy(tmp, 0, vertices, vertexBuffer.limit()
				- mesh.numVertices * INDICES_PER_VERTEX, mesh.numVertices
				* INDICES_PER_VERTEX);

		children.remove(mesh);
		children.add(mesh);

		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			currVertexIndex += child.getNumVertices();
		}
		dirty = true;
	}

	// expand the space allotted for the given mesh so it fits :)
	protected synchronized void expand(Mesh2D mesh, int oldSize, int newSize) {
		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			currVertexIndex += child.getNumVertices();
		}
		numVertices = currVertexIndex;
		vertices = Arrays.copyOf(vertices, numVertices * INDICES_PER_VERTEX);

		int src = (mesh.parentVertexIndex + oldSize) * INDICES_PER_VERTEX;
		int dst = (mesh.parentVertexIndex + newSize) * INDICES_PER_VERTEX;
		System.arraycopy(vertices, src, vertices, dst, vertices.length - dst);

		vertexBuffer = FloatBuffer.wrap(vertices);
	}

	private synchronized void updateBuffers() {
		initHandles();

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		View.gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.limit()
				* FLOAT_BYTES, vertexBuffer, GL11.GL_DYNAMIC_DRAW);
	}

	private void initHandles() {
		if (vertexHandle != -1) {
			return; // already initialized
		}
		int[] buffer = new int[1];

		View.gl.glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];
	}
}
