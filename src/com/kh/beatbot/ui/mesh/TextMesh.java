package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.texture.TextureGroup;

public class TextMesh extends Mesh {
	protected String text;

	public TextMesh(TextureGroup group, String text) {
		this.text = text;
		updateIndices();
		setGroup(group);
	}

	public synchronized void setText(String text, float x, float y, float height) {
		if (text.equals(this.text) && height == this.height) {
			setPosition(x, y);
			return;
		}

		this.x = x;
		this.y = y;
		this.height = height;
		String oldText = this.text;
		this.text = text;

		if (oldText.length() != text.length()) {
			updateIndices();
			group.changeSize(this, oldText.length() * 4, text.length() * 4, oldText.length()
					* Rectangle.FILL_INDICES.length, text.length() * Rectangle.FILL_INDICES.length);
		}

		((TextureGroup) group).setText(this, text, x, y, height);
	}

	private synchronized void updateIndices() {
		this.numVertices = text.length() * 4;
		indices = new short[Rectangle.FILL_INDICES.length * text.length()];
		for (short i = 0; i < text.length(); i++) {
			for (short j = 0; j < Rectangle.FILL_INDICES.length; j++) {
				indices[i * Rectangle.FILL_INDICES.length + j] = (short) (i * 4 + Rectangle.FILL_INDICES[j]);
			}
		}
	}
}
