package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Flanger extends Effect {

	public Flanger(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	public void initParams() {
		numParams = 6;
		effectNum = 4;
		if (params.isEmpty()) {
			params.add(new Param(true, false, "ms"));
			params.add(new Param(false, false, ""));
			params.add(new Param(false, false, ""));
			params.add(new Param(true, true, "Hz"));
			getParam(3).hz = true;
			params.add(new Param(false, false, ""));
			params.add(new Param(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.flanger_param_layout;
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.flanger_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.flanger_label_off;
	}
}
