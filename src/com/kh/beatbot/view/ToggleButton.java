package com.kh.beatbot.view;

import com.kh.beatbot.global.BBIconSource;

public class ToggleButton extends Button {
	boolean on = false;

	public ToggleButton(TouchableSurfaceView parent) {
		super(parent);
	}

	public void setIconSource(BBIconSource iconSource) {
		this.iconSource = iconSource;
		if (on)
			currentIcon = iconSource.selectedIcon;
		else {
			if (iconSource.disabledIcon != null) {
				currentIcon = iconSource.disabledIcon;
			} else {
				currentIcon = iconSource.defaultIcon;
			}
		}
	}

	protected void touch() {
		currentIcon = iconSource.pressedIcon != null ? iconSource.pressedIcon : iconSource.selectedIcon;
	}

	protected void release(boolean sendEvent) {
		if (sendEvent) {
			setOn(!on);
			notifyClicked();
		} else if (!on) {
			currentIcon = iconSource.defaultIcon;
		}
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
		currentIcon = on ? iconSource.selectedIcon : iconSource.defaultIcon;
	}
}
