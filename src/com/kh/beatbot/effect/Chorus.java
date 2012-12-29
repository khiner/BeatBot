package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Chorus extends Effect {

	public Chorus(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	protected void initParams() {
		effectNum = 0;
		numParams = 5;
		if (params.isEmpty()) {
			params.add(new EffectParam(true, true, "Hz"));
			getParam(0).hz = true;
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(true, true, "ms"));
			params.add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.chorus_param_layout;
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.chorus_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.chorus_label_off;
	}
}
