package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Delay extends Effect {
	public Delay(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	public float rightChannelLevelMemory = -1;
	public boolean rightChannelBeatSyncMemory = true;

	@Override
	public void initParams() {
		paramsLinked = true;
		numParams = 4;
		effectNum = 2;
		if (params.isEmpty()) {
			params.add(new Param(true, true, "ms"));
			params.add(new Param(true, true, "ms"));
			params.add(new Param(false, false, ""));
			params.add(new Param(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.delay_param_layout;
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.delay_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.delay_label_off;
	}

	@Override
	public void setParamsLinked(boolean linked) {
		super.setParamsLinked(linked);
		setEffectParam(trackNum, position, 4, linked ? 1 : 0);
	}
}
