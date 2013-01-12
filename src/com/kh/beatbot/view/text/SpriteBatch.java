package com.kh.beatbot.view.text;

import javax.microedition.khronos.opengles.GL10;

public class SpriteBatch {
	// number of characters to render per batch
	final static int CHAR_BATCH_SIZE = 100;
	// Vertex Size (in Components) ie. (X,Y,U,V)
	final static int VERTEX_SIZE = 4;
	final static int VERTICES_PER_SPRITE = 4; // Vertices Per Sprite
	final static int INDICES_PER_SPRITE = 6; // Indices Per Sprite
	
	GL10 gl; // GL Instance
	Vertices vertices; // Vertices Instance Used for Rendering
	float[] vertexBuffer; // Vertex Buffer
	int bufferIndex = 0; // Vertex Buffer Start Index
	int numSprites = 0; // Number of Sprites Currently in Buffer

	// D: prepare the sprite batcher for specified maximum number of sprites
	// A: gl - the gl instance to use for rendering
	// maxSprites - the maximum allowed sprites per batch
	public SpriteBatch(GL10 gl) {
		this.gl = gl; // Save GL Instance
		vertexBuffer = new float[CHAR_BATCH_SIZE * VERTICES_PER_SPRITE * VERTEX_SIZE];
		vertices = new Vertices(gl, CHAR_BATCH_SIZE * VERTICES_PER_SPRITE,
				CHAR_BATCH_SIZE * INDICES_PER_SPRITE);

		short[] indices = new short[CHAR_BATCH_SIZE * INDICES_PER_SPRITE];
		for (int i = 0, j = 0; i < indices.length; i += INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE) {
			indices[i + 0] = (short) (j + 0); // Calculate Index 0
			indices[i + 1] = (short) (j + 1); // Calculate Index 1
			indices[i + 2] = (short) (j + 2); // Calculate Index 2
			indices[i + 3] = (short) (j + 2); // Calculate Index 3
			indices[i + 4] = (short) (j + 3); // Calculate Index 4
			indices[i + 5] = (short) (j + 0); // Calculate Index 5
		}
		vertices.setIndices(indices, 0, indices.length);
	}

	public void beginBatch(int textureId) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId); // Bind the Texture
		numSprites = 0; // Empty Sprite Counter
		bufferIndex = 0; // Reset Buffer Index (Empty)
	}

	// D: signal the end of a batch. render the batched sprites
	public void endBatch() {
		if (numSprites > 0) { // IF Any Sprites to Render
			vertices.setVertices(vertexBuffer, 0, bufferIndex);
			vertices.draw(numSprites * INDICES_PER_SPRITE);
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
		if (numSprites >= vertexBuffer.length - 1) {
			endBatch(); // End Batch
			// NOTE: leave current texture bound!!
			numSprites = 0; // Empty Sprite Counter
			bufferIndex = 0; // Reset Buffer Index (Empty)
		}

		float halfWidth = width / 2.0f;
		float halfHeight = height / 2.0f;
		float x1 = x - halfWidth; // Left X
		float y1 = y - halfHeight; // Bottom Y
		float x2 = x + halfWidth; // Right X
		float y2 = y + halfHeight; // Top Y

		vertexBuffer[bufferIndex++] = x1; // Add X for Vertex 0
		vertexBuffer[bufferIndex++] = y1; // Add Y for Vertex 0
		vertexBuffer[bufferIndex++] = region.u1; // Add U for Vertex 0
		vertexBuffer[bufferIndex++] = region.v2; // Add V for Vertex 0

		vertexBuffer[bufferIndex++] = x2; // Add X for Vertex 1
		vertexBuffer[bufferIndex++] = y1; // Add Y for Vertex 1
		vertexBuffer[bufferIndex++] = region.u2; // Add U for Vertex 1
		vertexBuffer[bufferIndex++] = region.v2; // Add V for Vertex 1

		vertexBuffer[bufferIndex++] = x2; // Add X for Vertex 2
		vertexBuffer[bufferIndex++] = y2; // Add Y for Vertex 2
		vertexBuffer[bufferIndex++] = region.u2; // Add U for Vertex 2
		vertexBuffer[bufferIndex++] = region.v1; // Add V for Vertex 2

		vertexBuffer[bufferIndex++] = x1; // Add X for Vertex 3
		vertexBuffer[bufferIndex++] = y2; // Add Y for Vertex 3
		vertexBuffer[bufferIndex++] = region.u1; // Add U for Vertex 3
		vertexBuffer[bufferIndex++] = region.v1; // Add V for Vertex 3

		numSprites++; // Increment Sprite Count
	}
}
