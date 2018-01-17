package com.odang.beatbot.ui.texture;

import com.odang.beatbot.ui.mesh.Mesh;
import com.odang.beatbot.ui.mesh.MeshGroup;
import com.odang.beatbot.ui.mesh.TextMesh;
import com.odang.beatbot.ui.view.View;

public class TextureGroup extends MeshGroup {
    public final static int VERTICES_PER_TEXTURE = 4;

    public TextureGroup(int primitiveType, int[] textureId) {
        super(primitiveType, 8, textureId);
    }

    public synchronized void setText(TextMesh mesh, String text, float x, float y, float height) {
        final float scale = height / View.context.getFontTextureAtlas().getCellHeight();
        final float cellWidth = View.context.getFontTextureAtlas().getCellWidth() * scale;

        int textureIndex = 0;
        for (char character : text.toCharArray()) {
            TextureRegion charRegion = View.context.getFontTextureAtlas().getTextureRegion(character);
            textureVertices(mesh, textureIndex, charRegion, x, y, cellWidth, height, null);
            x += FontTextureAtlas.getCharWidth(character) * scale;
            textureIndex += VERTICES_PER_TEXTURE;
        }
        dirty = true;
    }

    public void setResource(Mesh mesh, int resourceId) {
        setTexture(mesh, View.context.getResourceTextureAtlas().getTextureRegion(resourceId));
        dirty = true;
    }

    private void textureVertices(Mesh mesh, int textureVertex, TextureRegion region,
                                 float x, float y, float width, float height, float[] color) {
        vertex(mesh, textureVertex, x, y + height, color, region.u1, region.v2);
        vertex(mesh, textureVertex + 1, x + width, y + height, color, region.u2, region.v2);
        vertex(mesh, textureVertex + 2, x + width, y, color, region.u2, region.v1);
        vertex(mesh, textureVertex + 3, x, y, color, region.u1, region.v1);
    }

    public void layout(Mesh mesh, float x, float y, float width, float height) {
        vertex(mesh, 0, x, y + height);
        vertex(mesh, 1, x + width, y + height);
        vertex(mesh, 2, x + width, y);
        vertex(mesh, 3, x, y);
    }

    private void setTexture(Mesh mesh, TextureRegion region) {
        textureVertex(mesh, 0, region.u1, region.v2);
        textureVertex(mesh, 1, region.u2, region.v2);
        textureVertex(mesh, 2, region.u2, region.v1);
        textureVertex(mesh, 3, region.u1, region.v1);
    }

    private void vertex(Mesh mesh, int index, float x, float y, float[] color,
                        float textureX, float textureY) {
        vertex(mesh, index, x, y, color);
        textureVertex(mesh, index, textureX, textureY);
    }

    private void textureVertex(Mesh mesh, int index, float textureX, float textureY) {
        int vertexIndex = getVertexIndex(mesh, index);
        vertices[vertexIndex + 6] = textureX;
        vertices[vertexIndex + 7] = textureY;
    }
}
