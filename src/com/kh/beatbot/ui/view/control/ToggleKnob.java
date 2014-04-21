package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.EffectParam;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;

public class ToggleKnob extends Knob implements ParamToggleListener {

	private ToggleButton centerButton;
	private float snapDistSquared;
	private boolean centerButtonTouched = false;

	public ToggleKnob(RenderGroup renderGroup) {
		super(renderGroup);
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		centerButton.setId(id);
	}

	@Override
	public synchronized void createChildren() {
		super.createChildren();
		centerButton = new ToggleButton(renderGroup, true);
		centerButton.setIcon(IconResourceSets.BEAT_SYNC);
		centerButton.setChecked(true);
		centerButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((EffectParam) param).toggle(((ToggleButton) button).isChecked());
			}
		});
		// not adding center button as child, instead manually drawing and
		// handling touch events
	}

	@Override
	public synchronized void layoutChildren() {
		super.layoutChildren();
		centerButton.layout(this, width / 16, width / 16, 7 * width / 8, 7 * width / 8);
		snapDistSquared = (width / 4) * (width / 4);
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		if (distanceFromCenterSquared(pos) < snapDistSquared) {
			centerButton.handleActionDown(id, pos);
			centerButtonTouched = true;
		} else {
			super.handleActionDown(id, pos);
		}
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		if (centerButtonTouched) {
			centerButton.handleActionMove(id, pos);
		} else {
			super.handleActionMove(id, pos);
		}
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		if (centerButtonTouched) {
			centerButton.handleActionUp(id, pos);
		} else {
			super.handleActionUp(id, pos);
		}
		centerButtonTouched = false;
	}

	@Override
	public void onParamToggled(EffectParam param) {
		centerButton.setChecked(param.isBeatSync());
	}
}
