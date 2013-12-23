package com.kh.beatbot.ui.mesh;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.util.Log;

import com.kh.beatbot.ui.view.View;

public class MeshGroup {
	private List<Mesh2D> children = new ArrayList<Mesh2D>();
	private FloatBuffer vertexBuffer, colorBuffer;
	private float[] vertices;
	private float[] colors;
	private int vertexHandle = -1, colorHandle = -1, numVertices = -1;
	private int primitiveType;
	private boolean dirty = false;

	public MeshGroup(int primitiveType) {
		this.primitiveType = primitiveType;
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
		if (mesh == null) {
			return;
		}
		children.add(mesh);
		updateVertices();
	}

	public synchronized void remove(Mesh2D mesh) {
		if (mesh == null) {
			return;
		} else if (!children.contains(mesh)) {
			Log.e("MeshGroup",
					"Attempting to remove a mesh that is not a child.");
			return;
		} else {
			children.remove(mesh);
			updateVertices();
		}
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
		updateVertices(newMesh);
	}

	private synchronized void updateVertices() {
		numVertices = calcNumVertices();

		vertices = new float[numVertices * 2];
		vertexBuffer = FloatBuffer.wrap(vertices);

		colors = new float[numVertices * 4];
		colorBuffer = FloatBuffer.wrap(colors);

		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			updateVertices(child);
			currVertexIndex += child.getNumVertices();
		}
	}

	public synchronized void updateVertices(Mesh2D child) {
		if (child == null) {
			return;
		} else if (!children.contains(child)) {
			Log.e("MeshGroup",
					"Attempting to update a mesh that is not a child.");
			return;
		}
		dirty = true;

		int vertexIndex = child.parentVertexIndex * 2;
		for (float vertex : child.vertices) {
			vertices[vertexIndex++] = vertex;
		}

		int colorIndex = child.parentVertexIndex;
		for (int i = colorIndex; i < colorIndex + child.numVertices; i++) {
			colors[i * 4] = child.getColor()[0];
			colors[i * 4 + 1] = child.getColor()[1];
			colors[i * 4 + 2] = child.getColor()[2];
			colors[i * 4 + 3] = child.getColor()[3];
		}
	}

	public synchronized void clear() {
		children.clear();
		updateVertices();
	}

	private synchronized void updateBuffers() {
		initHandles();

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		View.gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertices.length * 4,
				vertexBuffer, GL11.GL_DYNAMIC_DRAW);

		View.gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		View.gl.glBufferData(GL11.GL_ARRAY_BUFFER, colors.length * 4,
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

	private int calcNumVertices() {
		int totalVertices = 0;
		for (Mesh2D child : children) {
			totalVertices += child.getNumVertices();
		}

		return totalVertices;
	}
}
