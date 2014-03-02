package com.kh.beatbot.ui.texture;

import com.kh.beatbot.ui.mesh.Mesh;
import com.kh.beatbot.ui.mesh.MeshGroup;
import com.kh.beatbot.ui.mesh.TextMesh;

public class TextureGroup extends MeshGroup {
	private final static int VERTICES_PER_TEXTURE = 4;

	public TextureGroup(int primitiveType, int[] textureId) {
		super(primitiveType, 8, textureId);
	}

	public synchronized void setText(TextMesh mesh, String text, float x,
			float y, float height, float[] color) {
		final float scale = height / TextureAtlas.font.getCellHeight();

		int textureIndex = mesh.getParentVertexIndex();
		for (char character : text.toCharArray()) {
			textureVertices(textureIndex,
					TextureAtlas.font.getTextureRegion(character), x, y,
					TextureAtlas.font.getCellWidth() * scale, height, color);
			x += FontTextureAtlas.getCharWidth(character) * scale;
			textureIndex += VERTICES_PER_TEXTURE;
		}

		dirty = true;
	}

	public synchronized void setResource(Mesh mesh, int resourceId, float x,
			float y, float width, float height, float[] color) {
		if (-1 == resourceId)
			return;
		textureVertices(mesh.getParentVertexIndex(),
				TextureAtlas.resource.getTextureRegion(resourceId), x, y,
				width, height, color);

		dirty = true;
	}

	public synchronized void layout(Mesh mesh, float x, float y, float width,
			float height) {
		layout(mesh.getParentVertexIndex(), x, y, width, height);

		dirty = true;
	}

	public synchronized void setResource(Mesh mesh, int resourceId) {
		if (-1 == resourceId)
			return;
		setTexture(mesh.getParentVertexIndex(),
				TextureAtlas.resource.getTextureRegion(resourceId));

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

	private synchronized void layout(int textureIndex, float x, float y, float width, float height) {
		int i = textureIndex * indicesPerVertex;
		vertex(i, x, y + height);
		vertex(i + indicesPerVertex, x + width, y + height);
		vertex(i + indicesPerVertex * 2, x + width, y);
		vertex(i + indicesPerVertex * 3, x, y);
	}

	private synchronized void setTexture(int textureIndex, TextureRegion region) {
		int i = textureIndex * indicesPerVertex;
		textureVertex(i, region.u1, region.v2);
		textureVertex(i + indicesPerVertex, region.u2, region.v2);
		textureVertex(i + indicesPerVertex * 2, region.u2, region.v1);
		textureVertex(i + indicesPerVertex * 3, region.u1, region.v1);
	}

	private synchronized void vertex(int index, float x, float y,
			float[] color, float textureX, float textureY) {
		vertices[index] = x;
		vertices[index + 1] = y;
		if (null != color) {
			vertices[index + 2] = color[0];
			vertices[index + 3] = color[1];
			vertices[index + 4] = color[2];
			vertices[index + 5] = color[3];
		}
		vertices[index + 6] = textureX;
		vertices[index + 7] = textureY;
	}

	private synchronized void vertex(int index, float x, float y) {
		vertices[index] = x;
		vertices[index + 1] = y;
	}

	private synchronized void textureVertex(int index, float textureX, float textureY) {
		vertices[index + 6] = textureX;
		vertices[index + 7] = textureY;
	}
}
