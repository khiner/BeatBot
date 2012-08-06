package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	@Override
	public void initParamControls() {
		super.initParamControls();
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'y', "Bits"));
		}
	}
			
	public boolean isEffectOn() {
		return GlobalVars.effectOn[trackNum][EFFECT_NUM];
	}
	
	public void setEffectOnNative(boolean on) {
		setDecimateOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setDecimateParam(trackNum, paramNum, level);
	}
	
	@Override
	public int getEffectLayoutId() {
		return R.layout.decimate_layout;
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
