package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;

public class ToggleButton extends ImageButton {
	boolean checked = false;

	public void setIcon(Icon newIconSource) {
		super.setIcon(newIconSource);
		refreshIcons();
	}

	public void release() {
		pressed = false;
		refreshIcons();
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

	protected void refreshIcons() {
		for (Icon icon : icons) {
			if (icon != null) {
				icon.setState(enabled ? (checked ? IconResource.State.SELECTED
						: IconResource.State.DEFAULT)
						: IconResource.State.DISABLED);
			}
		}
	}
}
