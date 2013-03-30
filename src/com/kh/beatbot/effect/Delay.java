package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Delay extends Effect {
	
	public static final String NAME = GlobalVars.mainActivity.getString(R.string.delay);
	public static final int EFFECT_NUM = 4;
	public static final int NUM_PARAMS = 2;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("TIME LEFT", true, true, "ms"),
		new ParamData("TIME RIGHT", true, true, "ms"),
		new ParamData("FEEDBACK", false, false, ""),
		new ParamData("WET", false, false, "")
	};

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
	
	public ParamData[] getParamsData() {
		return PARAMS_DATA;
	}

	@Override
	public void setParamsLinked(boolean linked) {
		super.setParamsLinked(linked);
		setEffectParam(trackNum, position, EFFECT_NUM, linked ? 1 : 0);
	}
}
