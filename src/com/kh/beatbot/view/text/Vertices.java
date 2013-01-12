package com.kh.beatbot.view.text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Vertices {
	final static int COLOR_CNT = 4; // Number of Components in Vertex Color
	final static int TEXCOORD_CNT = 2; // Number of Components in Vertex Texture
										// Coords

	final static int INDEX_SIZE = Short.SIZE / 8; // Index Byte Size (Short.SIZE
													// = bits)

	// NOTE: all members are constant, and initialized in constructor!
	final GL10 gl; // GL Instance
	public final int vertexSize = (2 + TEXCOORD_CNT) * 4; // Bytesize of a Single Vertex
	final IntBuffer vertices; // Vertex Buffer
	final ShortBuffer indices; // Index Buffer

	final int[] tmpBuffer; // Temp Buffer for Vertex Conversion

	public Vertices(GL10 gl, int maxVertices, int maxIndices) {
		this.gl = gl; // Save GL Instance
		ByteBuffer buffer = ByteBuffer.allocateDirect(maxVertices * vertexSize);
		buffer.order(ByteOrder.nativeOrder()); // Set Native Byte Order
		this.vertices = buffer.asIntBuffer(); // Save Vertex Buffer
		buffer = ByteBuffer.allocateDirect(maxIndices * INDEX_SIZE);
		buffer.order(ByteOrder.nativeOrder()); // Set Native Byte Order
		this.indices = buffer.asShortBuffer(); // Save Index Buffer
		this.tmpBuffer = new int[maxVertices * vertexSize / 4];
	}

	// --Set Vertices--//
	// D: set the specified vertices in the vertex buffer
	// NOTE: optimized to use integer buffer!
	// A: vertices - array of vertices (floats) to set
	// offset - offset to first vertex in array
	// length - number of floats in the vertex array (total)
	// for easy setting use: vtx_cnt * (this.vertexSize / 4)
	// R: [none]
	public void setVertices(float[] vertices, int offset, int length) {
		this.vertices.clear(); // Remove Existing Vertices
		int last = offset + length; // Calculate Last Element
		for (int i = offset, j = 0; i < last; i++, j++)
			tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);
		this.vertices.put(tmpBuffer, 0, length); // Set New Vertices
		this.vertices.flip(); // Flip Vertex Buffer
	}

	// --Set Indices--//
	// D: set the specified indices in the index buffer
	// A: indices - array of indices (shorts) to set
	// offset - offset to first index in array
	// length - number of indices in array (from offset)
	// R: [none]
	public void setIndices(short[] indices, int offset, int length) {
		this.indices.clear(); // Clear Existing Indices
		this.indices.put(indices, offset, length); // Set New Indices
		this.indices.flip(); // Flip Index Buffer
	}

	// --Draw--//
	// D: draw the currently bound vertices in the vertex/index buffers
	// numVertices - the number of vertices (indices) to draw
	// R: [none]
	public void draw(int numVertices) {
		vertices.position(0);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		vertices.position(2);
		gl.glTexCoordPointer(TEXCOORD_CNT, GL10.GL_FLOAT, vertexSize,
					vertices); // Set Texture Coords Pointer
		indices.position(0); // Set Index Buffer to Specified Offset
		gl.glDrawElements(GL10.GL_TRIANGLES, numVertices,
				GL10.GL_UNSIGNED_SHORT, indices); // Draw Indexed
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
}
