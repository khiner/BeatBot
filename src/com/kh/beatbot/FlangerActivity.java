package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class FlangerActivity extends EffectActivity {
	@Override
	public void initParams() {
		EFFECT_NUM = GlobalVars.FLANGER_EFFECT_NUM;
		NUM_PARAMS = 6;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, false, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "Hz"));
			getParam(3).hz = true;
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.flanger_param_layout;
	}
}
