package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;

public class Tremelo extends Effect {

	public Tremelo(String name, int trackNum) {
		super(name, trackNum);
	}

	@Override
	public void initParams() {
		numParams = 3;
		if (GlobalVars.params[trackNum][effectNum].isEmpty()) {
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, true, "Hz"));
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
			getParam(0).hz = true;
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setTremeloParam(trackNum, paramNum, paramLevel);
	}
	
	@Override
	public int getParamLayoutId() {
		return R.layout.tremelo_param_layout;
	}
	
	@Override
	public int getOnDrawableId() {
		return R.drawable.tremelo_label_on;
	}
	
	@Override
	public int getOffDrawableId() {
		return R.drawable.tremelo_label_off;
	}

	@Override
	public void setEffectOnNative(boolean on) {
		setTremeloOn(trackNum, on);
	}
	
	public static native void setTremeloOn(int trackNum, boolean on);
	public static native void setTremeloParam(int trackNum, int paramNum,
			float param);
}
