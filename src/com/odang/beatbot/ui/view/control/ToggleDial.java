package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.listener.ParamToggleListener;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;

public class ToggleDial extends TouchableView implements ParamToggleListener {
	private Dial dial;
	private ToggleButton centerButton;
	private float snapDistSquared;

	public ToggleDial(View view) {
		super(view);
	}

	public Dial getDial() {
		return dial;
	}

	@Override
	public void setId(int id) {
		dial.setId(id);
		centerButton.setId(id);
	}

	@Override
	public void createChildren() {
		dial = new Dial(this);
		centerButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.BEAT_SYNC);
		centerButton.setChecked(true);
		centerButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((Param) dial.param).setBeatSync(((ToggleButton) button).isChecked());
			}
		});
	}

	@Override
	public void layoutChildren() {
		dial.layout(this, 0, 0, width, height);
		centerButton.layout(this, width / 16, width / 16, 7 * width / 8, 7 * width / 8);
		snapDistSquared = (width / 4) * (width / 4);
	}

	@Override
	public View findChildAt(float x, float y) {
		return distanceFromCenterSquared(x, y) < snapDistSquared ? centerButton : dial;
	}

	@Override
	public void onParamToggle(Param param) {
		centerButton.setChecked(param.isBeatSync());
	}
}
