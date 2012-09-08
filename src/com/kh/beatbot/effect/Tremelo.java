package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Tremelo extends Effect {

	public Tremelo(int id, String name, int trackNum) {
		super(id, name, trackNum);
	}

	@Override
	public void initParams() {
		numParams = 3;
		if (params.isEmpty()) {
			params.add(new EffectParam(true, true, "Hz"));
			params.add(new EffectParam(false, false, ""));
			getParam(0).hz = true;
			params.add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setTremeloParam(trackNum, paramNum, paramLevel);
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

	@Override
	public void setEffectOnNative(boolean on) {
		setTremeloOn(trackNum, on);
	}
	
	public static native void setTremeloOn(int trackNum, boolean on);
	public static native void setTremeloParam(int trackNum, int paramNum,
			float param);
}
