package com.kh.beatbot.ui.mesh;

public class TextMesh extends Mesh {
	protected String text;
	protected float height;

	public TextMesh(TextGroup group, String text) {
		setText(text);
		setGroup(group);
	}

	public void setText(String text, float x, float y, float height,
			float[] color) {
		this.height = height;
		String oldText = this.text;
		setText(text);

		if (oldText.length() != text.length()) {
			group.changeSize(this, oldText.length() * 4, numVertices);
		}

		((TextGroup)group).setText(this, text, x, y, height, color);
	}

	private void setText(String text) {
		this.text = text;
		numVertices = text.length() * 4;
	}
}
