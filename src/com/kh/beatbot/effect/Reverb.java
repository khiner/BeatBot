package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Reverb extends Effect {

	public Reverb(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	public void initParams() {
		numParams = 2;
		effectNum = 5;
		if (params.isEmpty()) {
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.reverb_param_layout;
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.reverb_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.reverb_label_off;
	}
}
