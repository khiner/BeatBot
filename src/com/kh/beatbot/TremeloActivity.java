package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class TremeloActivity extends EffectActivity {
	@Override
	public void initParams() {
		EFFECT_NUM = GlobalVars.TREMELO_EFFECT_NUM;
		NUM_PARAMS = 3;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "Hz"));
			getParam(0).scaleFactor = getParam(1).scaleFactor = 16;
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.tremelo_param_layout;
	}
}
