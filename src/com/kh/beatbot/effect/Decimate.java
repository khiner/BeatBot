package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Decimate extends Effect {

	public Decimate(int id, String name, int trackNum, int position) {
		super(id, name, trackNum, position);
	}

	@Override
	protected void initParams() {
		numParams = 2;
		effectNum = 1;
		if (params.isEmpty()) {
			params.add(new EffectParam(true, false, "Hz"));
			params.add(new EffectParam(true, false, "Bits"));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.decimate_param_layout;
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.bitcrush_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.bitcrush_label_off;
	}
}
