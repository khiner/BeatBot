package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Delay extends Effect {
	
	public static final String NAME = GlobalVars.mainActivity.getString(R.string.delay);
	public static final int EFFECT_NUM = 4;
	public static final int NUM_PARAMS = 2;

	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	public float rightChannelLevelMemory = -1;
	public boolean rightChannelBeatSyncMemory = true;
	public static boolean PARAMS_LINKED = true;
	
	public Delay(int trackNum, int position) {
		super(trackNum, position);
	}

	public int getNum() {
		return EFFECT_NUM;
	}

	public int numParams() {
		return NUM_PARAMS;
	}
	
	public String getName() {
		return NAME;
	}
	
	@Override
	public void initParams() {
		paramsLinked = true;
		if (params.isEmpty()) {
			params.add(new Param("TIME LEFT", true, true, "ms"));
			params.add(new Param("TIME RIGHT", true, true, "ms"));
			params.add(new Param("FEEDBACK", false, false, ""));
			params.add(new Param("WET", false, false, ""));
		}
	}

	@Override
	public void setParamsLinked(boolean linked) {
		super.setParamsLinked(linked);
		setEffectParam(trackNum, position, 4, linked ? 1 : 0);
	}
}
