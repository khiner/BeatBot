package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class DelayActivity extends EffectActivity {

	@Override
	public void initParams() {
		EFFECT_NUM = 2;
		NUM_PARAMS = 3;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
		}
	}

	public void setEffectOnNative(boolean on) {
		setDelayOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setDelayParam(trackNum, paramNum, level);
	}

	@Override
	public int getEffectLayoutId() {
		return R.layout.delay_layout;
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayParam(int trackNum, int paramNum, float param);
}
