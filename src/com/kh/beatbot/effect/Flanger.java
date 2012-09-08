package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Flanger extends Effect {

	public Flanger(int id, String name, int trackNum) {
		super(id, name, trackNum);
	}

	@Override
	public void initParams() {
		numParams = 6;
		if (params.isEmpty()) {
			params.add(new EffectParam(true, false, "ms"));
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(true, true, "Hz"));
			getParam(3).hz = true;
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setFlangerParam(trackNum, paramNum, paramLevel);
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

	@Override
	public void setEffectOnNative(boolean on) {
		setFlangerOn(trackNum, on);
	}
	
	public static native void setFlangerOn(int trackNum, boolean on);
	public static native void setFlangerParam(int trackNum, int paramNum,
			float param);
}
