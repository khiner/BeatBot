package com.kh.beatbot.ui.mesh;

public class TextMesh extends Mesh {
	protected String text;
	protected float height;

	public TextMesh(TextGroup group, String text) {
		setText(text);
		setGroup(group);
	}

	public synchronized void setText(String text, float x, float y,
			float height, float[] color) {
		this.height = height;
		String oldText = this.text;
		setText(text);

		if (oldText.length() != text.length()) {
			group.changeSize(this, oldText.length() * 4, numVertices,
					oldText.length() * RECT_INDICES.length, getNumIndices());
		}

		((TextGroup) group).setText(this, text, x, y, height, color);
	}

	private synchronized void setText(String text) {
		if (text.equals(this.text))
			return;
		this.text = text;
		numVertices = text.length() * 4;
		indices = new short[RECT_INDICES.length * text.length()];
		for (short i = 0; i < text.length(); i++) {
			for (short j = 0; j < RECT_INDICES.length; j++) {
				indices[i * RECT_INDICES.length + j] = (short) (i * 4 + RECT_INDICES[j]);
			}
		}
	}
}
