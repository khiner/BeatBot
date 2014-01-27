package com.kh.beatbot.ui.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import com.kh.beatbot.ui.view.View;

public class TextGroup {
	private List<TextMesh> children = new ArrayList<TextMesh>();

	private final static int VERTICES_PER_SPRITE = 4;
	private final static int INDICES_PER_SPRITE = 6;
	private final static int INDICES_PER_VERTEX = 8;
	private final static int SHORT_BYTES = Short.SIZE / 8;
	private final static int FLOAT_BYTES = Float.SIZE / 8;
	private final static int VERTEX_BYTES = INDICES_PER_VERTEX * FLOAT_BYTES;

	private boolean dirty = false;
	private int numChars = 0;
	private float[] vertices = new float[0];
	private FloatBuffer vertexBuffer;
	private ShortBuffer indexBuffer;

	public synchronized void initChar(int charIndex, float x, float y,
			float width, float height, TextureRegion region, float[] color) {
		int index = charIndex * VERTEX_BYTES;
		vertex(index, x, y + height, region.u1, region.v2, color);
		vertex(index + INDICES_PER_VERTEX, x + width, y + height, region.u2,
				region.v2, color);
		vertex(index + INDICES_PER_VERTEX * 2, x + width, y, region.u2,
				region.v1, color);
		vertex(index + INDICES_PER_VERTEX * 3, x, y, region.u1, region.v1,
				color);
	}

	public synchronized void add(TextMesh mesh) {
		if (mesh == null || children.contains(mesh)) {
			return;
		}
		mesh.parentCharIndex = numChars;
		children.add(mesh);
		numChars += mesh.getNumChars();
		vertices = Arrays.copyOf(vertices, numChars * VERTEX_BYTES);
	}

	public synchronized void remove(TextMesh mesh) {
		if (mesh == null) {
			return;
		} else if (!children.contains(mesh)) {
			Log.e("TextGroup",
					"Attempting to remove a mesh that is not a child.");
			return;
		}

		int dst = (mesh.parentCharIndex + mesh.getNumChars()) * VERTEX_BYTES;
		System.arraycopy(vertices, dst, vertices, mesh.parentCharIndex
				* VERTEX_BYTES, vertices.length - dst);

		children.remove(mesh);

		int currCharIndex = 0;
		for (TextMesh child : children) {
			child.parentCharIndex = currCharIndex;
			currCharIndex += child.getNumChars();
		}
		numChars = currCharIndex;

		vertices = Arrays.copyOf(vertices, numChars * VERTEX_BYTES);
		dirty = true;
	}

	// expand the space allotted for the given mesh so it fits :)
	protected synchronized void expand(TextMesh mesh, int oldSize, int newSize) {
		int currCharIndex = 0;
		for (TextMesh child : children) {
			child.parentCharIndex = currCharIndex;
			currCharIndex += child.getNumChars();
		}
		numChars = currCharIndex;

		vertices = Arrays.copyOf(vertices, numChars * VERTEX_BYTES);
		int src = (mesh.parentCharIndex + oldSize) * VERTEX_BYTES;
		int dst = (mesh.parentCharIndex + newSize) * VERTEX_BYTES;

		System.arraycopy(vertices, src, vertices, dst, vertices.length - dst);

		dirty = true;
	}

	// expand the space allotted for the given mesh so it fits :)
	protected synchronized void contract(TextMesh mesh, int oldSize, int newSize) {
		int currCharIndex = 0;
		for (TextMesh child : children) {
			child.parentCharIndex = currCharIndex;
			currCharIndex += child.getNumChars();
		}
		numChars = currCharIndex;

		int src = (mesh.parentCharIndex + oldSize) * VERTEX_BYTES;
		int dst = (mesh.parentCharIndex + newSize) * VERTEX_BYTES;
		System.arraycopy(vertices, src, vertices, dst, vertices.length - src);

		vertices = Arrays.copyOf(vertices, numChars * VERTEX_BYTES);

		dirty = true;
	}

	public synchronized void setText(TextMesh mesh, String text, float x,
			float y, float height, float[] color) {

		int charIndex = mesh.parentCharIndex;

		final float scale = height / GLText.getCellHeight();

		for (char character : text.toCharArray()) {
			initChar(charIndex++, x, y, GLText.getCellWidth() * scale, height,
					GLText.getCharRegion(character), color);
			x += GLText.getCharWidth(character) * scale;
		}

		dirty = true;
	}

	public synchronized void setColor(TextMesh mesh, float[] color) {
		for (int charIndex = mesh.parentCharIndex; charIndex < mesh.parentCharIndex
				+ mesh.getNumChars(); charIndex++) {
			for (int vertexIndex = 0; vertexIndex < VERTICES_PER_SPRITE; vertexIndex++) {
				System.arraycopy(color, 0, vertices, charIndex * VERTEX_BYTES
						+ vertexIndex * INDICES_PER_VERTEX + 4, color.length);
			}
		}

		dirty = true;
	}

	public synchronized void draw() {
		if (children.isEmpty())
			return;

		if (dirty) {
			updateIndices();
			dirty = false;
		}

		GL10 gl = View.gl;
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, GLText.getTextureId());

		vertexBuffer.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_BYTES, vertexBuffer);
		vertexBuffer.position(2);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_BYTES, vertexBuffer);
		vertexBuffer.position(4);
		gl.glColorPointer(4, GL10.GL_FLOAT, VERTEX_BYTES, vertexBuffer);

		gl.glDrawElements(GL10.GL_TRIANGLES, numChars * INDICES_PER_SPRITE,
				GL10.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	private synchronized void vertex(int index, float x, float y,
			float textureX, float textureY, float[] color) {
		vertices[index] = x;
		vertices[index + 1] = y;
		vertices[index + 2] = textureX;
		vertices[index + 3] = textureY;
		vertices[index + 4] = color[0];
		vertices[index + 5] = color[1];
		vertices[index + 6] = color[2];
		vertices[index + 7] = color[3];
	}

	private synchronized void updateIndices() {
		short[] indices = new short[numChars * INDICES_PER_SPRITE];
		for (int i = 0, j = 0; i < indices.length; i += INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE) {
			indices[i] = (short) j;
			indices[i + 1] = (short) (j + 1);
			indices[i + 2] = (short) (j + 2);
			indices[i + 3] = (short) (j + 2);
			indices[i + 4] = (short) (j + 3);
			indices[i + 5] = (short) j;
		}

		indexBuffer = createShortBuffer(indices);
		vertexBuffer = createFloatBuffer(vertices);
	}

	private FloatBuffer createFloatBuffer(float[] floats) {
		FloatBuffer fb = ByteBuffer.allocateDirect(floats.length * FLOAT_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		fb.clear();
		fb.put(floats);
		fb.position(0);

		return fb;
	}

	private ShortBuffer createShortBuffer(short[] shorts) {
		ShortBuffer sb = ByteBuffer.allocateDirect(shorts.length * SHORT_BYTES)
				.order(ByteOrder.nativeOrder()).asShortBuffer();
		sb.clear();
		sb.put(shorts);
		sb.position(0);

		return sb;
	}
}
