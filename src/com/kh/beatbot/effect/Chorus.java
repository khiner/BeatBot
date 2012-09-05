package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;

public class Chorus extends Effect {

	public Chorus(String name, int trackNum) {
		super(name, trackNum);
	}

	@Override
	protected void initParams() {
		numParams = 5;	
		if (GlobalVars.params[trackNum][effectNum].isEmpty()) {
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, true, "Hz"));
			getParam(0).hz = true;
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, true, "ms"));
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
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

	@Override
	public void setEffectOnNative(boolean on) {
		setChorusOn(trackNum, on);
	}
	
	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setChorusParam(trackNum, paramNum, paramLevel);
	}
	
	public static native void setChorusOn(int trackNum, boolean on);
	public static native void setChorusParam(int trackNum, int paramNum,
			float param);
}
