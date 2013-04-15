package com.kh.beatbot.view.control;

import com.kh.beatbot.global.IconSource;

public class ToggleButton extends ImageButton {
	boolean checked = false;
	
	public void setIconSource(IconSource iconSource) {
		this.iconSource = iconSource;
		if (iconSource != null) {
			iconSource.setState(checked ? IconSource.State.SELECTED : IconSource.State.DEFAULT);
		}
	}

	public void release() {
		pressed = false;
		if (iconSource != null) {
			iconSource.setState(checked ? IconSource.State.SELECTED : IconSource.State.DEFAULT);
		}
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		release();
	}

	@Override
	protected void notifyClicked() {
		setChecked(!checked);
		super.notifyClicked();
	}
}
