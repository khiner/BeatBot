package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Decimate extends Effect {

	public Decimate(int id, String name, int trackNum) {
		super(id, name, trackNum);
	}

	@Override
	protected void initParams() {
		numParams = 2;
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

	@Override
	public void setEffectOnNative(boolean on) {
		setDecimateOn(trackNum, on);
	}
	
	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setDecimateParam(trackNum, paramNum, paramLevel);
	}
	
	public static native void setDecimateOn(int trackNum, boolean on);
	public static native void setDecimateParam(int trackNum, int paramNum,
			float param);
}
