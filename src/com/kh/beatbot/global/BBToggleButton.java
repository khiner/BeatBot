package com.kh.beatbot.global;

public class BBToggleButton extends BBButton {
	boolean on = false;
	boolean touched = false;

	public BBToggleButton(BBIconSource iconSource) {
		super(iconSource);
	}

	public BBToggleButton(BBIconSource iconSource, float height) {
		super(iconSource, height);
	}

	public BBToggleButton(BBIconSource iconSource, float width, float height) {
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
