package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.View;

public class ToggleButton extends Button {
	private boolean oscillating = false, checked = false;

	public ToggleButton(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
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
