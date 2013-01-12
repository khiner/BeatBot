package com.kh.beatbot.view.text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SpriteBatch {
	// number of characters to render per batch
	final static int CHAR_BATCH_SIZE = 100;
	final static int VERTICES_PER_SPRITE = 4; // Vertices Per Sprite
	final static int INDICES_PER_SPRITE = 6; // Indices Per Sprite
	final static int VERTEX_SIZE = 16; // Bytesize of a Single Vertex
	
	private int vertexBufferIndex = 0;
	private int numSprites = 0; // Number of Sprites Currently in Buffer
	private float[] vertices;
	
	private GL10 gl;
	private FloatBuffer vertexBuffer;
	private ShortBuffer indices;

	// D: prepare the sprite batcher for specified maximum number of sprites
	// A: gl - the gl instance to use for rendering
	// maxSprites - the maximum allowed sprites per batch
	public SpriteBatch(GL10 gl) {
		this.gl = gl; // Save GL Instance
		vertices = new float[CHAR_BATCH_SIZE * VERTICES_PER_SPRITE * 4];
		ByteBuffer buffer = ByteBuffer.allocateDirect(CHAR_BATCH_SIZE * VERTICES_PER_SPRITE * VERTEX_SIZE);
		buffer.order(ByteOrder.nativeOrder());
		vertexBuffer = buffer.asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(CHAR_BATCH_SIZE * INDICES_PER_SPRITE * Short.SIZE / 8);
		buffer.order(ByteOrder.nativeOrder());
		indices = buffer.asShortBuffer();

		short[] shortIndices = new short[CHAR_BATCH_SIZE * INDICES_PER_SPRITE];
		for (int i = 0, j = 0; i < shortIndices.length; i += INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE) {
			shortIndices[i] = (short) j;
			shortIndices[i + 1] = (short) (j + 1);
			shortIndices[i + 2] = (short) (j + 2);
			shortIndices[i + 3] = (short) (j + 2);
			shortIndices[i + 4] = (short) (j + 3);
			shortIndices[i + 5] = (short) j;
		}
		indices.clear();
		indices.put(shortIndices, 0, shortIndices.length);
	}

	public void beginBatch(int textureId) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId); // Bind the Texture
		numSprites = 0; // Empty Sprite Counter
		vertexBufferIndex = 0; // Reset Buffer Index (Empty)
	}

	// D: signal the end of a batch. render the batched sprites
	public void endBatch() {
		if (numSprites > 0) {
			this.vertexBuffer.clear(); // Remove Existing Vertices
			this.vertexBuffer.put(vertices, 0, vertexBufferIndex); // Set New Vertices
			vertexBuffer.position(0);
			gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, vertexBuffer);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			vertexBuffer.position(2);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, vertexBuffer);
			indices.position(0);
			gl.glDrawElements(GL10.GL_TRIANGLES, numSprites * INDICES_PER_SPRITE,
					GL10.GL_UNSIGNED_SHORT, indices);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	// D: batch specified sprite to batch. adds vertices for sprite to vertex
	// buffer
	// NOTE: MUST be called after beginBatch(), and before endBatch()!
	// NOTE: if the batch overflows, this will render the current batch, restart
	// it, and then batch this sprite.
	// A: x, y - the x,y position of the sprite (center)
	// width, height - the width and height of the sprite
	// region - the texture region to use for sprite
	public void drawSprite(float x, float y, float width, float height,
			TextureRegion region) {
		if (numSprites >= vertices.length - 1) {
			endBatch(); // End Batch
			// NOTE: leave current texture bound!!
			numSprites = 0;
			vertexBufferIndex = 0;
		}

		float halfWidth = width / 2.0f;
		float halfHeight = height / 2.0f;
		float x1 = x - halfWidth; // Left X
		float y1 = y - halfHeight; // Bottom Y
		float x2 = x + halfWidth; // Right X
		float y2 = y + halfHeight; // Top Y

		vertices[vertexBufferIndex++] = x1; // Add X for Vertex 0
		vertices[vertexBufferIndex++] = y1; // Add Y for Vertex 0
		vertices[vertexBufferIndex++] = region.u1; // Add U for Vertex 0
		vertices[vertexBufferIndex++] = region.v2; // Add V for Vertex 0

		vertices[vertexBufferIndex++] = x2; // Add X for Vertex 1
		vertices[vertexBufferIndex++] = y1; // Add Y for Vertex 1
		vertices[vertexBufferIndex++] = region.u2; // Add U for Vertex 1
		vertices[vertexBufferIndex++] = region.v2; // Add V for Vertex 1

		vertices[vertexBufferIndex++] = x2; // Add X for Vertex 2
		vertices[vertexBufferIndex++] = y2; // Add Y for Vertex 2
		vertices[vertexBufferIndex++] = region.u2; // Add U for Vertex 2
		vertices[vertexBufferIndex++] = region.v1; // Add V for Vertex 2

		vertices[vertexBufferIndex++] = x1; // Add X for Vertex 3
		vertices[vertexBufferIndex++] = y2; // Add Y for Vertex 3
		vertices[vertexBufferIndex++] = region.u1; // Add U for Vertex 3
		vertices[vertexBufferIndex++] = region.v1; // Add V for Vertex 3

		numSprites++; // Increment Sprite Count
	}
}
