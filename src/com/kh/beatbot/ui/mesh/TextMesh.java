package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.texture.TextureGroup;

public class TextMesh extends Mesh {
	protected String text;

	public TextMesh(TextureGroup group) {
		this.group = group;
		show();
	}

	public void setText(String text) {
		if (null == text)
			return;

		String oldText = this.text;
		this.text = text;

		if (null == oldText || oldText.length() != text.length()) {
			group.changeSize(this, getNumVertices(oldText), getNumVertices(),
					getNumIndices(oldText), getNumIndices());
		}
		((TextureGroup) group).setText(this, text, x, y, height);
	}

	public void layout(float x, float y, float height) {
		if (!isVisible())
			return;
		this.x = x;
		this.y = y;
		this.height = height;
		((TextureGroup) group).setText(this, text, x, y, height);
	}

	@Override
	public int getNumIndices() {
		return getNumIndices(text);
	}

	@Override
	public int getNumVertices() {
		return getNumVertices(text);
	}

	@Override
	public short getIndex(int i) {
		short offset = (short) ((i / Rectangle.FILL_INDICES.length) * TextureGroup.VERTICES_PER_TEXTURE);
		return (short) (Rectangle.FILL_INDICES[i % Rectangle.FILL_INDICES.length] + offset);
	}

	private int getNumIndices(String text) {
		return null == text ? 0 : text.length() * Rectangle.FILL_INDICES.length;
	}

	private int getNumVertices(String text) {
		return null == text ? 0 : text.length() * TextureGroup.VERTICES_PER_TEXTURE;
	}
}
