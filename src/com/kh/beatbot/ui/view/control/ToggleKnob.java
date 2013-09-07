package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.EffectParam;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;

public class ToggleKnob extends Knob implements ParamToggleListener {

	private ToggleButton centerButton;
	private float snapDistSquared;
	private boolean centerButtonTouched = false;

	@Override
	public void setId(int id) {
		super.setId(id);
		centerButton.setId(id);
	}

	@Override
	public void draw() {
		super.draw();
		centerButton.draw();
	}

	protected void loadIcons() {
		super.loadIcons();
		centerButton.setIcon(new Icon(IconResources.BEAT_SYNC));
		centerButton.setChecked(true);
	}

	@Override
	protected void createChildren() {
		super.createChildren();
		centerButton = new ToggleButton();
		centerButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((EffectParam)param).toggle(((ToggleButton)button).isChecked());
			}
		});
		// not adding center button as child, instead manually drawing and
		// handling touch events
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();
		centerButton.layout(this, width / 16, width / 16, 7 * width / 8, 7 * width / 8);
		snapDistSquared = (width / 4) * (width / 4);
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (distanceFromCenterSquared(x, y) < snapDistSquared) {
			centerButton.handleActionDown(id, x, y);
			centerButtonTouched = true;
		} else {
			super.handleActionDown(id, x, y);
		}
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (centerButtonTouched) {
			centerButton.handleActionMove(id, x, y);
		} else {
			super.handleActionMove(id, x, y);
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		if (centerButtonTouched) {
			centerButton.handleActionUp(id, x, y);
		} else {
			super.handleActionUp(id, x, y);
		}
		centerButtonTouched = false;
	}

	@Override
	public void onParamToggled(EffectParam param) {
		centerButton.setChecked(param.isBeatSync());
	}
}
