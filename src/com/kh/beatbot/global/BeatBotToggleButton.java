package com.kh.beatbot.global;

public class BeatBotToggleButton extends BeatBotButton {
	boolean on = false;
	boolean touched = false;

	public BeatBotToggleButton(BeatBotIconSource iconSource) {
		super(iconSource);
	}
	
	public BeatBotToggleButton(BeatBotIconSource iconSource, float height) {
		super(iconSource, height);
	}
	
	public BeatBotToggleButton(BeatBotIconSource iconSource, float width, float height) {
		super(iconSource, width, height);
	}

	@Override
	public void touch() {
		// don't change icon on touch event for toggle button.
		// wait for release to toggle icon
		touched = true;
	}

	@Override
	public void release() {
		touched = false;
	}

	public void toggle() {
		on = !on;
		updateIcon();
	}
	
	public boolean isOn() {
		return on;
	}
	
	public void setOn(boolean on) {
		this.on = on;
		updateIcon();
	}
	
	private void updateIcon() {
		currentIcon = on ? selectedIcon : defaultIcon;
	}
}
