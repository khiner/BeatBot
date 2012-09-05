package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.PlaybackManager;

public class Filter extends Effect {
	
	public Filter(String name, int trackNum) {
		super(name, trackNum);
	}

	@Override
	public void initParams() {
		numParams = 4;
		if (GlobalVars.params[trackNum][effectNum].isEmpty()) {
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, false, "Hz"));
			getParam(0).scale = PlaybackManager.SAMPLE_RATE / 2;
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(true, true, "Hz"));
			getParam(2).hz = true;
			GlobalVars.params[trackNum][effectNum].add(new EffectParam(false, false, ""));
		}
	}

	public int getFilterMode() {
		return GlobalVars.filterMode[trackNum];
	}
	
	public void setFilterOn(int mode) {
		GlobalVars.filterMode[trackNum] = mode;
		setFilterOn(trackNum, on, mode); 
	}
	
	@Override
	public void setEffectOnNative(boolean on) {
		setFilterOn(trackNum, on, GlobalVars.filterMode[trackNum]);
	}
	
	@Override
	public void setParamNative(int paramNum, float paramLevel) {
		setFilterParam(trackNum, paramNum, paramLevel);
	}
	
	@Override
	public int getParamLayoutId() {
		return R.layout.filter_param_layout;
	}
	
	@Override
	public int getOnDrawableId() {
		return R.drawable.filter_label_on;
	}
	
	@Override
	public int getOffDrawableId() {
		return R.drawable.filter_label_off;
	}
	
	public native void setFilterOn(int trackNum, boolean on, int mode);
	public native void setFilterParam(int trackNum, int paramNum,
			float param);
}
