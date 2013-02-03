package com.kh.beatbot.global;

import com.kh.beatbot.view.TouchableSurfaceView;

public class BBToggleButton extends BBButton {
	boolean on = false;

	public BBToggleButton(TouchableSurfaceView parent) {
		super(parent);
	}

	public void setIconSource(BBIconSource iconSource) {
		defaultIcon = iconSource.defaultIcon;
		selectedIcon = iconSource.selectedIcon;
		if (on)
			currentIcon = selectedIcon;
		else
			currentIcon = defaultIcon;
	}

	protected void touch() {
		currentIcon = selectedIcon;
		requestRender();
	}

	protected void release(boolean sendEvent) {
		if (sendEvent) {
			setOn(!on);
			notifyClicked();
		} else if (!on) {
			currentIcon = defaultIcon;
		}
		requestRender();
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
		currentIcon = on ? selectedIcon : defaultIcon;
	}
}
