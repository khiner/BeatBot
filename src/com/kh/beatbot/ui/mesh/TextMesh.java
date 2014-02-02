package com.kh.beatbot.ui.mesh;

public class TextMesh extends Mesh {
	protected String text;
	protected float height;

	public TextMesh(TextGroup group, String text) {
		this.text = text;
		updateIndices();
		setGroup(group);
	}

	public synchronized void setText(String text, float x, float y,
			float height, float[] color) {
		this.height = height;
		String oldText = this.text;
		this.text = text;

		if (oldText.length() != text.length()) {
			updateIndices();
			group.changeSize(this, oldText.length() * 4, numVertices,
					oldText.length() * Rectangle.FILL_INDICES.length,
					getNumIndices());
		}

		((TextGroup) group).setText(this, text, x, y, height, color);
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
