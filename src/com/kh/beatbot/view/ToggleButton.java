package com.kh.beatbot.view;

import com.kh.beatbot.global.IconSource;

public class ToggleButton extends ImageButton {
	boolean checked = false;
	
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

	public void release() {
		super.release();
		currentIcon = checked ? iconSource.selectedIcon != null ? iconSource.selectedIcon : iconSource.pressedIcon : iconSource.defaultIcon;
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		release();
	}

	public void press() {
		super.press();
		currentIcon = iconSource.pressedIcon != null ? iconSource.pressedIcon
				: checked ? iconSource.defaultIcon : iconSource.selectedIcon;
	}

	@Override
	protected void notifyClicked() {
		setChecked(!checked);
		super.notifyClicked();
	}
}
