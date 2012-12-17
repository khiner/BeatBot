package com.kh.beatbot.global;


public class BeatBotButton {
	BeatBotIcon defaultIcon;
	BeatBotIcon selectedIcon;
	BeatBotIcon currentIcon;
	private float width, height;
	
	public BeatBotButton(BeatBotIconSource iconSource) {
		this(iconSource, iconSource.defaultIcon.getWidth(), iconSource.defaultIcon.getHeight());
	}
	
	public BeatBotButton(BeatBotIconSource iconSource, float height) {
		this(iconSource, iconSource.defaultIcon.getWidth() * height / iconSource.defaultIcon.getHeight(), height);
	}
	
	public BeatBotButton(BeatBotIconSource iconSource, float width, float height) {
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
	
	public void setIconSource(BeatBotIconSource iconSource) {
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
