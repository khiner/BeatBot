package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class FlangerActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, ' ', "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
		}
	}

	public void setEffectOnNative(boolean on) {
		setFlangerOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setFlangerParam(trackNum, paramNum, level);
	}

	@Override
	public int getEffectLayoutId() {
		return R.layout.flanger_layout;
	}
	
	public native void setFlangerOn(int trackNum, boolean on);
	public native void setFlangerParam(int trackNum, int paramNum, float param);
}
