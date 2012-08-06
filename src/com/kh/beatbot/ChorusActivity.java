package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class ChorusActivity extends EffectActivity {

	@Override
	public void initParamControls() {
		super.initParamControls();
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, ' ', "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
		}
	}

	public void setEffectOnNative(boolean on) {
		setChorusOn(trackNum, on);
	}

	public void setParamNative(int paramNum, float level) {
		setChorusParam(trackNum, paramNum, scaleLevel(level));
	}

	public native void setChorusOn(int trackNum, boolean on);

	public native void setChorusParam(int trackNum, int paramNum, float param);

	@Override
	public int getEffectLayoutId() {
		return R.layout.chorus_layout;
	}
}
