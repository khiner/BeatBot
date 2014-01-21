package com.kh.beatbot.ui.view.text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.view.View;

public class SpriteBatch {
	private final static int CHAR_BATCH_SIZE = 128, VERTICES_PER_SPRITE = 4,
			INDICES_PER_SPRITE = 6, VERTEX_BYTES = 32;

	private int vertexBufferIndex = 0, numSprites = 0;
	private float[] vertices;

	private FloatBuffer vertexBuffer;
	private ShortBuffer indices;

	// D: prepare the sprite batcher for specified maximum number of sprites
	// A: gl - the gl instance to use for rendering
	// maxSprites - the maximum allowed sprites per batch
	public SpriteBatch() {
		vertices = new float[CHAR_BATCH_SIZE * VERTICES_PER_SPRITE * 8];
		ByteBuffer buffer = ByteBuffer.allocateDirect(CHAR_BATCH_SIZE
				* VERTICES_PER_SPRITE * VERTEX_BYTES);
		buffer.order(ByteOrder.nativeOrder());
		vertexBuffer = buffer.asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(CHAR_BATCH_SIZE * INDICES_PER_SPRITE
				* Short.SIZE / 8);
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
		indices.position(0);
	}

	public void beginBatch() {
		numSprites = 0;
		vertexBufferIndex = 0;
	}

	public void endBatch() {
		vertexBuffer.clear();
		vertexBuffer.put(vertices, 0, vertexBufferIndex);
	}

	public void render(int textureId) {
		GL10 gl = View.gl;
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

		vertexBuffer.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_BYTES, vertexBuffer);
		vertexBuffer.position(2);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_BYTES, vertexBuffer);
		vertexBuffer.position(4);
		gl.glColorPointer(4, GL10.GL_FLOAT, VERTEX_BYTES, vertexBuffer);

		gl.glDrawElements(GL10.GL_TRIANGLES, numSprites * INDICES_PER_SPRITE,
				GL10.GL_UNSIGNED_SHORT, indices);

		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	public void initSprite(float x, float y, float width, float height,
			TextureRegion region, float[] color) {
		vertex(x, y + height, region.u1, region.v2, color);
		vertex(x + width, y + height, region.u2, region.v2, color);
		vertex(x + width, y, region.u2, region.v1, color);
		vertex(x, y, region.u1, region.v1, color);

		numSprites++;
	}

	private void vertex(float x, float y, float textureX, float textureY,
			float[] color) {
		vertices[vertexBufferIndex++] = x;
		vertices[vertexBufferIndex++] = y;
		vertices[vertexBufferIndex++] = textureX;
		vertices[vertexBufferIndex++] = textureY;
		vertices[vertexBufferIndex++] = color[0];
		vertices[vertexBufferIndex++] = color[1];
		vertices[vertexBufferIndex++] = color[2];
		vertices[vertexBufferIndex++] = color[3];
	}
}
