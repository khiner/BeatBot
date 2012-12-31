package com.kh.beatbot.global;

public class BBButton {
	BBIcon defaultIcon = null;
	BBIcon selectedIcon = null;
	BBIcon currentIcon = null;
	private float width, height;

	public BBButton(BBIconSource iconSource) {
		this(iconSource, iconSource.defaultIcon.getWidth(),
				iconSource.defaultIcon.getHeight());
	}

	public BBButton(BBIconSource iconSource, float height) {
		this(iconSource, iconSource.defaultIcon.getWidth() * height
				/ iconSource.defaultIcon.getHeight(), height);
	}

	public BBButton(BBIconSource iconSource, float width, float height) {
		defaultIcon = iconSource.defaultIcon;
		selectedIcon = iconSource.selectedIcon;
		currentIcon = defaultIcon;
		this.width = width;
		this.height = height;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setIconSource(BBIconSource iconSource) {
		defaultIcon = iconSource.defaultIcon;
		selectedIcon = iconSource.selectedIcon;
		currentIcon = defaultIcon;
	}

	public void touch() {
		currentIcon = selectedIcon;
	}

	public void release() {
		currentIcon = defaultIcon;
	}

	public void draw(float x, float y) {
		currentIcon.draw(x, y, width, height);
	}

	public void draw(float x, float y, float width, float height) {
		currentIcon.draw(x, y, width, height);
	}

	public float getIconWidth() {
		return currentIcon.getWidth();
	}

	public float getIconHeight() {
		return currentIcon.getHeight();
	}

	public boolean isTouched() {
		return currentIcon.equals(selectedIcon);
	}
}
