package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class ChorusActivity extends EffectActivity {

	@Override
	protected void initParams() {
		EFFECT_NUM = 0;
		NUM_PARAMS = 5;	
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ""));
		}
	}

	public void setEffectOnNative(boolean on) {
		setChorusOn(trackNum, on);
	}

	public float setParamNative(int paramNum, float level) {
		if (paramNum == 0)
			level *= 16;
		setChorusParam(trackNum, paramNum, level);
		return level;
	}

	public native void setChorusOn(int trackNum, boolean on);

	public native void setChorusParam(int trackNum, int paramNum, float param);

	@Override
	public int getParamLayoutId() {
		return R.layout.chorus_param_layout;
	}
}
