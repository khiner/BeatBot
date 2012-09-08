package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Reverb extends Effect {

	public Reverb(int id, String name, int trackNum) {
		super(id, name, trackNum);
	}

	@Override
	public void initParams() {
		numParams = 2;
		if (params.isEmpty()) {
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setReverbParam(trackNum, paramNum, paramLevel);
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

	@Override
	public void setEffectOnNative(boolean on) {
		setReverbOn(trackNum, on);
	}
	
	public static native void setReverbOn(int trackNum, boolean on);
	public static native void setReverbParam(int trackNum, int paramNum,
			float param);
}
