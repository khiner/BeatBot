package com.kh.beatbot.view;

import com.kh.beatbot.global.IconSource;

public class ToggleButton extends ImageButton {
	boolean checked = false;

	public ToggleButton(TouchableSurfaceView parent) {
		super(parent);
	}

	public void setIconSource(IconSource iconSource) {
		this.iconSource = iconSource;
		if (checked)
			currentIcon = iconSource.selectedIcon;
		else {
			if (iconSource.disabledIcon != null) {
				currentIcon = iconSource.disabledIcon;
			} else {
				currentIcon = iconSource.defaultIcon;
			}
		}
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		currentIcon = checked ? iconSource.selectedIcon
				: iconSource.defaultIcon;
	}

	protected void touch() {
		touched = true;
		currentIcon = iconSource.pressedIcon != null ? iconSource.pressedIcon
				: checked ? iconSource.defaultIcon : iconSource.selectedIcon;
	}

	protected void release(boolean sendEvent) {
		touched = false;
		if (sendEvent) {
			setChecked(!checked);
			notifyClicked();
		} else {
			currentIcon = checked ? iconSource.selectedIcon
					: iconSource.defaultIcon;
		}
	}
}
