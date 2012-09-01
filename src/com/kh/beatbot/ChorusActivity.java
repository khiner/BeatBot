package com.kh.beatbot;

import com.kh.beatbot.global.GlobalVars;

public class ChorusActivity extends EffectActivity {

	@Override
	protected void initParams() {
		EFFECT_NUM = GlobalVars.CHORUS_EFFECT_NUM;
		NUM_PARAMS = 5;	
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "Hz"));
			getParam(0).hz = true;
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.chorus_param_layout;
	}
	
	@Override
	public int getOnDrawableId() {
		return R.drawable.chorus_label_on;
	}
	
	@Override
	public int getOffDrawableId() {
		return R.drawable.chorus_label_off;
	}
}
