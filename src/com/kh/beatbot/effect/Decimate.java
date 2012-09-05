package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;

public class Decimate extends Effect {

	public Decimate(String name, int trackNum) {
		super(name, trackNum);
	}

	@Override
	protected void initParams() {
		numParams = 2;
		if (GlobalVars.params[trackNum][effectNum].isEmpty()) {
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, false, "Hz"));
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, false, "Bits"));
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
