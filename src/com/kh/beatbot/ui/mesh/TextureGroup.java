package com.kh.beatbot.ui.mesh;

public class TextureGroup extends MeshGroup {
	private final static int VERTICES_PER_TEXTURE = 4;

	public TextureGroup(int primitiveType, int[] textureId) {
		super(primitiveType, 8, textureId);
	}

	public synchronized void setText(TextMesh mesh, String text, float x,
			float y, float height, float[] color) {
		final float scale = height / GLText.getCellHeight();

		int textureIndex = mesh.parentVertexIndex;
		for (char character : text.toCharArray()) {
			textureVertices(textureIndex, GLText.getCharRegion(character), x,
					y, GLText.getCellWidth() * scale, height, color);
			x += GLText.getCharWidth(character) * scale;
			textureIndex += VERTICES_PER_TEXTURE;
		}

		dirty = true;
	}

	public synchronized void setResource(Mesh mesh, int resourceId, float x,
			float y, float width, float height, float[] color) {
		textureVertices(mesh.parentVertexIndex,
				TextureAtlas.getTextureRegion(resourceId), x, y, width, height,
				color);

		dirty = true;
	}

	private synchronized void textureVertices(int textureIndex,
			TextureRegion region, float x, float y, float width, float height,
			float[] color) {
		int i = textureIndex * indicesPerVertex;
		vertex(i, x, y + height, color, region.u1, region.v2);
		vertex(i + indicesPerVertex, x + width, y + height, color, region.u2,
				region.v2);
		vertex(i + indicesPerVertex * 2, x + width, y, color, region.u2,
				region.v1);
		vertex(i + indicesPerVertex * 3, x, y, color, region.u1, region.v1);
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
