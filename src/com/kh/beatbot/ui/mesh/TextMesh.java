package com.kh.beatbot.ui.mesh;

public class TextMesh {
	protected TextGroup group;
	protected String text;
	protected int parentCharIndex = -1;

	public TextMesh(TextGroup group, String text) {
		this.text = text;
		setGroup(group);
	}

	public void setGroup(TextGroup group) {
		if (this.group == group)
			return;
		if (null != this.group) {
			this.group.remove(this);
		}
		this.group = group;
		this.group.add(this);
	}

	public void destroy() {
		if (group == null)
			return;
		group.remove(this);
		group = null;
	}

	public int getNumChars() {
		return text.length();
	}

	public void setText(String text, float x, float y, float height,
			float[] color) {
		String oldText = this.text;
		this.text = text;
		if (oldText.length() < text.length()) {
			group.expand(this, oldText.length(), text.length());
		} else if (oldText.length() > text.length()) {
			group.contract(this, oldText.length(), text.length());
		}

		group.setText(this, text, x, y, height, color);
	}
	
	public void setColor(float[] color) {
		group.setColor(this, color);
	}
}
