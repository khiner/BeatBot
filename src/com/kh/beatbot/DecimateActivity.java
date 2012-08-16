package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	@Override
	protected void initParams() {
		EFFECT_NUM = 1;
		NUM_PARAMS = 2;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, "Bits"));
		}
	}
			
	public boolean isEffectOn() {
		return GlobalVars.effectOn[trackNum][EFFECT_NUM];
	}
	
	public void setEffectOnNative(boolean on) {
		setDecimateOn(trackNum, on);
	}

	@Override
	public float setParamNative(int paramNum, float level) {
		setDecimateParam(trackNum, paramNum, level);
		return level;
	}
	
	@Override
	public int getParamLayoutId() {
		return R.layout.decimate_param_layout;
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
