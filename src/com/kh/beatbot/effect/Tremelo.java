package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Tremelo extends Effect {

	public Tremelo(int id, String name, int trackNum, int position) {
		super(id, name, trackNum, position);
	}

	@Override
	public void initParams() {
		numParams = 3;
		effectNum = 6;
		if (params.isEmpty()) {
			params.add(new EffectParam(true, true, "Hz"));
			params.add(new EffectParam(false, false, ""));
			getParam(0).hz = true;
			params.add(new EffectParam(false, false, ""));
		}
	}
	
	@Override
	public int getParamLayoutId() {
		return R.layout.tremelo_param_layout;
	}
	
	@Override
	public int getOnDrawableId() {
		return R.drawable.tremelo_label_on;
	}
	
	@Override
	public int getOffDrawableId() {
		return R.drawable.tremelo_label_off;
	}
}
