package com.kh.beatbot.ui.mesh;

public class TextGroup extends MeshGroup {
	private final static int VERTICES_PER_SPRITE = 4;

	public TextGroup(int primitiveType) {
		super(primitiveType, 8, true);
	}

	public synchronized void setText(TextMesh mesh, String text, float x,
			float y, float height, float[] color) {
		final float scale = height / GLText.getCellHeight();

		int charIndex = mesh.parentVertexIndex / VERTICES_PER_SPRITE;
		for (char character : text.toCharArray()) {
			initChar(charIndex++, x, y, GLText.getCellWidth() * scale, height,
					GLText.getCharRegion(character), color);
			x += GLText.getCharWidth(character) * scale;
		}

		dirty = true;
	}

	private synchronized void initChar(int charIndex, float x, float y,
			float width, float height, TextureRegion region, float[] color) {
		int index = charIndex * VERTICES_PER_SPRITE * indicesPerVertex;
		vertex(index, x, y + height, color, region.u1, region.v2);
		vertex(index + indicesPerVertex, x + width, y + height, color,
				region.u2, region.v2);
		vertex(index + indicesPerVertex * 2, x + width, y, color, region.u2,
				region.v1);
		vertex(index + indicesPerVertex * 3, x, y, color, region.u1, region.v1);
	}

	private synchronized void vertex(int index, float x, float y,
			float[] color, float textureX, float textureY) {
		vertices[index] = x;
		vertices[index + 1] = y;
		vertices[index + 2] = color[0];
		vertices[index + 3] = color[1];
		vertices[index + 4] = color[2];
		vertices[index + 5] = color[3];
		vertices[index + 6] = textureX;
		vertices[index + 7] = textureY;
	}
}
