package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	@Override
	protected void initParams() {
		EFFECT_NUM = GlobalVars.DECIMATE_EFFECT_NUM;
		NUM_PARAMS = 2;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, false, "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, false, "Bits"));
		}
	}
			
	public boolean isEffectOn() {
		return GlobalVars.effectOn[trackNum][EFFECT_NUM];
	}
	
	@Override
	public int getParamLayoutId() {
		return R.layout.decimate_param_layout;
	}
}
