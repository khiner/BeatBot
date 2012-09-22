package com.kh.beatbot.global;


public class BeatBotButton {
	BeatBotIcon defaultIcon;
	BeatBotIcon selectedIcon;
	BeatBotIcon currentIcon;
	public BeatBotButton(BeatBotIconSource iconSource) {
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
		currentIcon.draw(x, y);
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
