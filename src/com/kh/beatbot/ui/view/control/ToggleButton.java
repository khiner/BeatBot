package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.icon.IconResource;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.view.View;

public class ToggleButton extends Button {
	private boolean oscillating = false, checked = false;

	public ToggleButton(View view) {
		super(view);
	}

	public ToggleButton oscillating() {
		oscillating = true;
		return this;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		setState(checked ? State.SELECTED : State.DEFAULT);
	}

	/*
	 * Set the state to 'checked' and notify any listeners to trigger an event
	 */
	public void trigger(boolean checked) {
		setChecked(checked);
		super.notifyReleased();
	}

	@Override
	protected IconResource getIconResource() {
		if (oscillating && isPressed() && null == icon.getResource(State.PRESSED)) {
			return icon.getResource(checked ? State.DEFAULT : State.SELECTED);
		} else {
			return super.getIconResource();
		}
	}

	@Override
	protected void notifyReleased() {
		if (oscillating || !checked) {
			setChecked(!checked);
		}
		super.notifyReleased();
	}

	@Override
	public void release() {
		releaseLongPress();
		setChecked(checked);
	}

	@Override
	public synchronized ToggleButton withIcon(IconResourceSet resourceSet) {
		return (ToggleButton) super.withIcon(resourceSet);
	}

	@Override
	public synchronized ToggleButton withRect() {
		return (ToggleButton) super.withRect();
	}
	
	@Override
	public synchronized ToggleButton withRoundedRect() {
		return (ToggleButton) super.withRoundedRect();
	}
}
