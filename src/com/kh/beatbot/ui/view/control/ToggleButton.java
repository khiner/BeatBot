package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.mesh.ShapeGroup;

public class ToggleButton extends Button {
	boolean checked = false;
	boolean oscillating = false;

	public ToggleButton(ShapeGroup shapeGroup, boolean oscillating) {
		super(shapeGroup);
		this.oscillating = oscillating;
	}

	public void release() {
		pressed = false;
		releaseLongPress();
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
		if (oscillating || !checked)
			setChecked(!checked);
		super.notifyReleased();
	}
}
