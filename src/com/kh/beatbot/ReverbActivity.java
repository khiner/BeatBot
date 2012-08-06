package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	@Override
	public void initParamControls() {
		super.initParamControls();
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'x', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'y', ""));
		}
	}
	
	public void setEffectOnNative(boolean on) {
		setReverbOn(trackNum, on);
	}
	
	@Override
	public void setParamNative(int paramNum, float level) {
		setReverbParam(trackNum, paramNum, level);
	}
	
	@Override
	public int getEffectLayoutId() {
		return R.layout.reverb_layout;
	}
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbParam(int trackNum, int paramNum, float feedback);
}
