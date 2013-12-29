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
	private List<Mesh2D> children = new ArrayList<Mesh2D>();
	private FloatBuffer vertexBuffer, colorBuffer;
	private float[] vertices = new float[0], colors = new float[0];
	private int vertexHandle = -1, colorHandle = -1, numVertices = 0,
			primitiveType;
	private boolean dirty = false;

	public MeshGroup(int primitiveType) {
		this.primitiveType = primitiveType;
	}

	protected float getVertexX(int index) {
		return vertices[index * 2];
	}

	protected float getVertexY(int index) {
		return vertices[index * 2 + 1];
	}

	protected synchronized void vertex(Mesh2D mesh, float x, float y,
			float[] color) {
		int index = mesh.index + mesh.parentVertexIndex;
		int vertexOffset = index * 2;
		vertices[vertexOffset] = x;
		vertices[vertexOffset + 1] = y;
		System.arraycopy(color, 0, colors, index * 4, 4);
		dirty = true;
	}

	protected synchronized void translate(Mesh2D mesh, float x, float y) {
		for (int i = mesh.parentVertexIndex * 2; i < (mesh.parentVertexIndex + mesh.numVertices) * 2; i += 2) {
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
		View.gl.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

		View.gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		View.gl.glColorPointer(4, GL10.GL_FLOAT, 0, 0);

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
		vertices = Arrays.copyOf(vertices, numVertices * 2);
		colors = Arrays.copyOf(colors, numVertices * 4);
		vertexBuffer = FloatBuffer.wrap(vertices);
		colorBuffer = FloatBuffer.wrap(colors);
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

		int dst = mesh.parentVertexIndex + mesh.numVertices;
		System.arraycopy(vertices, dst * 2, vertices,
				mesh.parentVertexIndex * 2, vertices.length - dst * 2);
		System.arraycopy(colors, dst * 4, colors, mesh.parentVertexIndex * 4,
				colors.length - dst * 4);

		children.remove(mesh);

		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			currVertexIndex += child.getNumVertices();
		}
		numVertices = currVertexIndex;
		vertexBuffer.limit(numVertices * 2);
		colorBuffer.limit(numVertices * 4);
		dirty = true;
	}

	public synchronized void replace(Mesh2D oldMesh, Mesh2D newMesh) {
		if (newMesh == null) {
			remove(oldMesh);
			return;
		}
		if (oldMesh == null) {
			add(newMesh);
			return;
		}
		if (oldMesh.getNumVertices() != newMesh.getNumVertices()) {
			Log.e("MeshGroup",
					"Attempting to replace a mesh with a new one with different num vertices");
			return;
		}
		if (!children.contains(oldMesh)) {
			Log.e("MeshGroup",
					"Attempting to update a mesh that is not a child.");
			return;
		}

		newMesh.parentVertexIndex = oldMesh.parentVertexIndex;
		children.set(children.indexOf(oldMesh), newMesh);
	}

	public synchronized void push(Mesh2D mesh) {
		if (mesh == null || !children.contains(mesh))
			return;

		int dst = mesh.parentVertexIndex + mesh.numVertices;

		// move vertices to the end of the array
		float[] tmp = Arrays.copyOfRange(vertices, mesh.parentVertexIndex * 2,
				dst * 2);
		System.arraycopy(vertices, dst * 2, vertices,
				mesh.parentVertexIndex * 2, vertexBuffer.limit() - dst * 2);
		System.arraycopy(tmp, 0, vertices, vertexBuffer.limit()
				- mesh.numVertices * 2, mesh.numVertices * 2);

		// move colors to the end of the array
		tmp = Arrays.copyOfRange(colors, mesh.parentVertexIndex * 4, dst * 4);
		System.arraycopy(colors, dst * 4, colors, mesh.parentVertexIndex * 4,
				colorBuffer.limit() - dst * 4);
		System.arraycopy(tmp, 0, colors, colorBuffer.limit() - mesh.numVertices
				* 4, mesh.numVertices * 4);

		children.remove(mesh);
		children.add(mesh);

		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			currVertexIndex += child.getNumVertices();
		}
		dirty = true;
	}

	private synchronized void updateBuffers() {
		initHandles();

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		View.gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.limit() * 4,
				vertexBuffer, GL11.GL_DYNAMIC_DRAW);

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		View.gl.glBufferData(GL11.GL_ARRAY_BUFFER, colorBuffer.limit() * 4,
				colorBuffer, GL11.GL_DYNAMIC_DRAW);
	}

	private void initHandles() {
		if (vertexHandle != -1 && colorHandle != -1) {
			return; // already initialized
		}
		int[] buffer = new int[1];

		View.gl.glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];

		View.gl.glGenBuffers(1, buffer, 0);
		colorHandle = buffer[0];
	}
}
