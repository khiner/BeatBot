package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class TremeloActivity extends EffectActivity {
	@Override
	public void initParams() {
		EFFECT_NUM = 6;
		NUM_PARAMS = 2;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'y', ""));
		}
	}
	
	public void setEffectOnNative(boolean on) {
		setTremeloOn(trackNum, on);
	}

	@Override
	public float setParamNative(int paramNum, float level) {
		if (paramNum == 0)
			level *= 16; // mod rate has max of 16 Hz
		setTremeloParam(trackNum, paramNum, level);
		return level;
	}
	
	@Override
	public int getEffectLayoutId() {
		return R.layout.tremelo_layout;
	}
	
	public native void setTremeloOn(int trackNum, boolean on);
	public native void setTremeloParam(int trackNum, int paramNum, float param);
}
