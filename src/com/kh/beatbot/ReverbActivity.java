package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	@Override
	public void initParams() {
		EFFECT_NUM = GlobalVars.REVERB_EFFECT_NUM;
		NUM_PARAMS = 2;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.reverb_param_layout;
	}
}
