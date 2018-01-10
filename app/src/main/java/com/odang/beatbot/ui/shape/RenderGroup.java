package com.odang.beatbot.ui.shape;

import com.odang.beatbot.ui.mesh.MeshGroup;
import com.odang.beatbot.ui.texture.TextureGroup;
import com.odang.beatbot.ui.view.View;

import javax.microedition.khronos.opengles.GL10;

public class RenderGroup {
    private MeshGroup fillGroup, strokeGroup;
    private TextureGroup textureGroup, textGroup;

    public RenderGroup() {
        fillGroup = new MeshGroup(GL10.GL_TRIANGLE_STRIP);
        strokeGroup = new MeshGroup(GL10.GL_LINES);
        textureGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, View.context
                .getResourceTextureAtlas().getTextureId());
        textGroup = new TextureGroup(GL10.GL_TRIANGLE_STRIP, View.context.getFontTextureAtlas()
                .getTextureId());
    }

    public TextureGroup getTextureGroup() {
        return textureGroup;
    }

    public TextureGroup getTextGroup() {
        return textGroup;
    }

    public MeshGroup getFillGroup() {
        return fillGroup;
    }

    public MeshGroup getStrokeGroup() {
        return strokeGroup;
    }

    public void draw() {
        fillGroup.draw();
        strokeGroup.draw();
        textureGroup.draw();
        textGroup.draw();
    }

    public void translate(float x, float y) {
        fillGroup.translate(x, y);
        strokeGroup.translate(x, y);
        textureGroup.translate(x, y);
        textGroup.translate(x, y);
    }

    public void scale(float x, float y) {
        fillGroup.scale(x, y);
        strokeGroup.scale(x, y);
        textureGroup.scale(x, y);
        textGroup.scale(x, y);
    }
}