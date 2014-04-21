package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.shape.RenderGroup;

public class ToggleButton extends Button {
	boolean oscillating = false;
	boolean checked = false;

	public ToggleButton(RenderGroup renderGroup, boolean oscillating) {
		super(renderGroup);
		this.oscillating = oscillating;
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
		super.notifyRelease();
	}

	@Override
	protected void notifyRelease() {
		if (oscillating || !checked) {
			setChecked(!checked);
		}
		super.notifyRelease();
	}

	@Override
	public void release() {
		releaseLongPress();
		setChecked(checked);
	}
}
