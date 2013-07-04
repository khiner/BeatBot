package com.kh.beatbot.view.control;

import com.kh.beatbot.global.IconSource;

public class ToggleButton extends ImageButton {
	boolean checked = false;

	public void setIconSource(IconSource newIconSource) {
		super.setIconSource(newIconSource);
		for (IconSource iconSource : iconSources) {
			if (iconSource != null) {
				iconSource
						.setState(enabled ? (checked ? IconSource.State.SELECTED
								: IconSource.State.DEFAULT)
								: IconSource.State.DISABLED);
			}
		}
	}

	public void release() {
		pressed = false;
		for (IconSource iconSource : iconSources) {
			if (iconSource != null) {
				iconSource
						.setState(enabled ? (checked ? IconSource.State.SELECTED
								: IconSource.State.DEFAULT)
								: IconSource.State.DISABLED);
			}
		}
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		release();
	}

	/*
	 * Set the state to 'checked' and notify any listeners to trigger an event
	 */
	public void trigger(boolean checked) {
		setChecked(checked);
		super.notifyReleased();
	}
	
	@Override
	protected void notifyReleased() {
		setChecked(!checked);
		super.notifyReleased();
	}
}
