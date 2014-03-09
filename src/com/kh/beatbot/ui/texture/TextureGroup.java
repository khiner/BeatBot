package com.kh.beatbot.ui.texture;

import com.kh.beatbot.ui.mesh.Mesh;
import com.kh.beatbot.ui.mesh.MeshGroup;
import com.kh.beatbot.ui.mesh.TextMesh;

public class TextureGroup extends MeshGroup {
	private final static int VERTICES_PER_TEXTURE = 4;

	public TextureGroup(int primitiveType, int[] textureId) {
		super(primitiveType, 8, textureId);
	}

	public synchronized void setText(TextMesh mesh, String text, float x, float y, float height) {
		setText(mesh, text, x, y, height, null);
	}

	public synchronized void setText(TextMesh mesh, String text, float x, float y, float height,
			float[] color) {
		final float scale = height / TextureAtlas.font.getCellHeight();
		final float cellWidth = TextureAtlas.font.getCellWidth() * scale;

		int textureIndex = 0;
		for (char character : text.toCharArray()) {
			TextureRegion charRegion = TextureAtlas.font.getTextureRegion(character);
			textureVertices(mesh, textureIndex, charRegion, x, y, cellWidth, height, color);
			x += FontTextureAtlas.getCharWidth(character) * scale;
			textureIndex += VERTICES_PER_TEXTURE;
		}

		dirty = true;
	}

	public synchronized void setResource(Mesh mesh, int resourceId, float x, float y, float width,
			float height, float[] color) {
		if (-1 == resourceId)
			return;
		textureVertices(mesh, 0, TextureAtlas.resource.getTextureRegion(resourceId), x, y, width,
				height, color);

		dirty = true;
	}

	public synchronized void setResource(Mesh mesh, int resourceId) {
		if (-1 == resourceId)
			return;
		setTexture(mesh, TextureAtlas.resource.getTextureRegion(resourceId));

		dirty = true;
	}

	private synchronized void textureVertices(Mesh mesh, int textureVertex, TextureRegion region,
			float x, float y, float width, float height, float[] color) {
		vertex(mesh, textureVertex, x, y + height, color, region.u1, region.v2);
		vertex(mesh, textureVertex + 1, x + width, y + height, color, region.u2, region.v2);
		vertex(mesh, textureVertex + 2, x + width, y, color, region.u2, region.v1);
		vertex(mesh, textureVertex + 3, x, y, color, region.u1, region.v1);
	}

	public synchronized void layout(Mesh mesh, float x, float y, float width, float height) {
		vertex(mesh, 0, x, y + height);
		vertex(mesh, 1, x + width, y + height);
		vertex(mesh, 2, x + width, y);
		vertex(mesh, 3, x, y);
	}

	private synchronized void setTexture(Mesh mesh, TextureRegion region) {
		textureVertex(mesh, 0, region.u1, region.v2);
		textureVertex(mesh, 1, region.u2, region.v2);
		textureVertex(mesh, 2, region.u2, region.v1);
		textureVertex(mesh, 3, region.u1, region.v1);
	}

	private synchronized void vertex(Mesh mesh, int index, float x, float y, float[] color,
			float textureX, float textureY) {
		vertex(mesh, index, x, y, color);
		textureVertex(mesh, index, textureX, textureY);
	}

	private synchronized void textureVertex(Mesh mesh, int index, float textureX, float textureY) {
		int vertex = (mesh.parentVertexIndex + index) * indicesPerVertex;

		vertices[vertex + 6] = textureX;
		vertices[vertex + 7] = textureY;
	}
}
