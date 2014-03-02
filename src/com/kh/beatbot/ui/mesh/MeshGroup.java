package com.kh.beatbot.ui.mesh;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.ui.view.View;

import android.util.Log;

public abstract class MeshGroup {
	public final static int SHORT_BYTES = Short.SIZE / 8;
	public final static int FLOAT_BYTES = Float.SIZE / 8;

	protected final static int COLOR_OFFSET = 2;
	protected final static int COLOR_OFFSET_BYTES = COLOR_OFFSET * FLOAT_BYTES;

	protected final static int TEX_OFFSET = 6;
	protected final static int TEX_OFFSET_BYTES = TEX_OFFSET * FLOAT_BYTES;

	protected boolean dirty = false;
	protected int indicesPerVertex, vertexBytes, primitiveType,
			vertexHandle = -1, indexHandle = -1;

	protected short[] indices = new short[0];
	protected float[] vertices = new float[0];
	protected FloatBuffer vertexBuffer;
	protected ShortBuffer indexBuffer;

	protected int[] textureId;

	protected List<Mesh> children = new ArrayList<Mesh>();

	protected MeshGroup(int primitiveType, int indicesPerVertex) {
		this(primitiveType, indicesPerVertex, null);
	}

	protected MeshGroup(int primitiveType, int indicesPerVertex, int[] textureId) {
		this.primitiveType = primitiveType;
		this.indicesPerVertex = indicesPerVertex;
		this.vertexBytes = this.indicesPerVertex * FLOAT_BYTES;
		this.textureId = textureId;
	}

	public synchronized void draw() {
		if (children.isEmpty())
			return;

		if (dirty) {
			updateBuffers();
			dirty = false;
		}

		GL11 gl = View.getGl();
		if (hasTexture()) {
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId[0]);
		}

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glVertexPointer(2, GL10.GL_FLOAT, vertexBytes, 0);
		gl.glColorPointer(4, GL10.GL_FLOAT, vertexBytes, COLOR_OFFSET_BYTES);
		if (hasTexture()) {
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, vertexBytes,
					TEX_OFFSET_BYTES);
		}

		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indexHandle);
		gl.glDrawElements(primitiveType, indexBuffer.limit(),
				GL10.GL_UNSIGNED_SHORT, 0);

		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		if (hasTexture()) {
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public synchronized boolean contains(Mesh mesh) {
		return children.contains(mesh);
	}

	public synchronized void add(Mesh mesh) {
		if (null == mesh || children.contains(mesh)) {
			return;
		}
		vertices = Arrays.copyOf(vertices,
				vertices.length + (mesh.getNumVertices() * indicesPerVertex));

		indices = Arrays.copyOf(indices, indices.length + mesh.getNumIndices());

		children.add(mesh);
		resetIndices();

		vertexBuffer = FloatBuffer.wrap(vertices);
		indexBuffer = ShortBuffer.wrap(indices);

		dirty = true;
	}

	public synchronized void remove(Mesh mesh) {
		if (null == mesh) {
			return;
		} else if (!children.contains(mesh)) {
			Log.e("MeshGroup",
					"Attempting to remove a mesh that is not a child.");
			return;
		}

		int src = (mesh.parentVertexIndex + mesh.numVertices)
				* indicesPerVertex;
		int dst = mesh.parentVertexIndex * indicesPerVertex;

		System.arraycopy(vertices, src, vertices, dst, vertices.length - src);

		children.remove(mesh);
		resetIndices();

		vertices = Arrays.copyOf(vertices,
				vertices.length - (mesh.getNumVertices() * indicesPerVertex));
		indices = Arrays.copyOf(indices, indices.length - mesh.getNumIndices());

		vertexBuffer = FloatBuffer.wrap(vertices);
		indexBuffer = ShortBuffer.wrap(indices);
		dirty = true;
	}

	public synchronized void push(Mesh2D mesh) {
		if (null == mesh || !children.contains(mesh))
			return;

		int src = mesh.parentVertexIndex * indicesPerVertex;
		int dst = (mesh.parentVertexIndex + mesh.numVertices)
				* indicesPerVertex;

		// move vertices to the end of the array
		float[] tmp = Arrays.copyOfRange(vertices, src, dst);
		System.arraycopy(vertices, dst, vertices, src, vertexBuffer.limit()
				- dst);
		System.arraycopy(tmp, 0, vertices, vertexBuffer.limit()
				- mesh.numVertices * indicesPerVertex, mesh.numVertices
				* indicesPerVertex);

		children.remove(mesh);
		children.add(mesh);

		resetIndices();
		dirty = true;
	}

	protected float getVertexX(int index) {
		return vertices[index * indicesPerVertex];
	}

	protected float getVertexY(int index) {
		return vertices[index * indicesPerVertex + 1];
	}

	protected synchronized void vertex(Mesh mesh, int index, float x, float y) {
		vertex(mesh, index, x, y, null);
	}

	protected synchronized void vertex(Mesh mesh, int index, float x, float y,
			float[] color) {
		int vertex = (mesh.parentVertexIndex + index) * indicesPerVertex;

		vertices[vertex] = x;
		vertices[vertex + 1] = y;
		if (null != color) {
			vertices[vertex + 2] = color[0];
			vertices[vertex + 3] = color[1];
			vertices[vertex + 4] = color[2];
			vertices[vertex + 5] = color[3];
		}

		dirty = true;
	}

	protected synchronized void setColor(Mesh mesh, float[] color) {
		for (int vertexIndex = 0; vertexIndex < mesh.getNumVertices(); vertexIndex++) {
			setColor(mesh, vertexIndex, color);
		}
		dirty = true;
	}

	protected synchronized void setColor(Mesh mesh, int vertexIndex,
			float[] color) {
		int offset = (mesh.parentVertexIndex + vertexIndex) * indicesPerVertex
				+ COLOR_OFFSET;
		vertices[offset] = color[0];
		vertices[offset + 1] = color[1];
		vertices[offset + 2] = color[2];
		vertices[offset + 3] = color[3];

		dirty = true;
	}

	protected synchronized void translate(Mesh mesh, float x, float y) {
		for (int i = mesh.parentVertexIndex * indicesPerVertex; i < (mesh.parentVertexIndex + mesh.numVertices)
				* indicesPerVertex; i += indicesPerVertex) {
			vertices[i] += x;
			vertices[i + 1] += y;
		}
		dirty = true;
	}

	protected synchronized void changeSize(Mesh mesh, int oldSize, int newSize,
			int oldNumIndices, int newNumIndices) {
		if (oldSize == newSize)
			return;

		int src = (mesh.parentVertexIndex + oldSize) * indicesPerVertex;
		int dst = (mesh.parentVertexIndex + newSize) * indicesPerVertex;

		if (newSize > oldSize) {
			vertices = Arrays.copyOf(vertices, vertices.length
					+ (newSize - oldSize) * indicesPerVertex);
			System.arraycopy(vertices, src, vertices, dst, vertices.length
					- dst);

			indices = Arrays.copyOf(indices, indices.length
					+ (newNumIndices - oldNumIndices));
			resetIndices(mesh);
		} else {
			System.arraycopy(vertices, src, vertices, dst, vertices.length
					- src);
			vertices = Arrays.copyOf(vertices, vertices.length
					- (oldSize - newSize) * indicesPerVertex);

			indices = Arrays.copyOf(indices, indices.length
					- (oldNumIndices - newNumIndices));
			resetIndices(mesh);
		}

		resetIndices();

		vertexBuffer = FloatBuffer.wrap(vertices);
		indexBuffer = ShortBuffer.wrap(indices);
		dirty = true;
	}

	private synchronized void resetIndices() {
		int numVertices = 0;
		int numIndices = 0;
		for (Mesh child : children) {
			child.parentVertexIndex = numVertices;
			if (child.parentIndexOffset != numIndices) {
				child.parentIndexOffset = numIndices;
				resetIndices(child);
			}
			numVertices += child.getNumVertices();
			numIndices += child.getNumIndices();
		}
	}

	private synchronized void resetIndices(Mesh mesh) {
		short[] childIndices = mesh.getIndices();
		for (int i = 0; i < childIndices.length; i++) {
			indices[mesh.parentIndexOffset + i] = (short) (childIndices[i] + mesh.parentVertexIndex);
		}
	}

	private synchronized void updateBuffers() {
		initHandles();

		GL11 gl = View.getGl();
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.limit()
				* FLOAT_BYTES, vertexBuffer, GL11.GL_DYNAMIC_DRAW);

		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indexHandle);
		gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.limit()
				* SHORT_BYTES, indexBuffer, GL11.GL_DYNAMIC_DRAW);
	}

	private void initHandles() {
		if (vertexHandle != -1) {
			return; // already initialized
		}
		int[] buffer = new int[1];

		GL11 gl = View.getGl();
		gl.glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];

		gl.glGenBuffers(1, buffer, 0);
		indexHandle = buffer[0];
	}

	private boolean hasTexture() {
		return null != textureId;
	}
}
