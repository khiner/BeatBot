package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class ToggleKnob extends TouchableView implements ParamToggleListener {
	private Knob knob;
	private ToggleButton centerButton;
	private float snapDistSquared;

	public ToggleKnob(View view) {
		super(view);
	}

	public Knob getKnob() {
		return knob;
	}

	@Override
	public void setId(int id) {
		knob.setId(id);
		centerButton.setId(id);
	}

	@Override
	public synchronized void createChildren() {
		knob = new Knob(this);
		centerButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.BEAT_SYNC);
		centerButton.setChecked(true);
		centerButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((Param) knob.param).setBeatSync(((ToggleButton) button).isChecked());
			}
		});
	}

	@Override
	public synchronized void layoutChildren() {
		knob.layout(this, 0, 0, width, height);
		centerButton.layout(this, width / 16, width / 16, 7 * width / 8, 7 * width / 8);
		snapDistSquared = (width / 4) * (width / 4);
	}

	@Override
	public synchronized View findChildAt(float x, float y) {
		return distanceFromCenterSquared(x, y) < snapDistSquared ? centerButton : knob;
	}

	@Override
	public void onParamToggled(Param param) {
		centerButton.setChecked(param.isBeatSync());
	}
}
