package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.GlobalVars;

public class Delay extends Effect {
	
	public static final String NAME = GlobalVars.mainActivity.getString(R.string.delay);
	public static final int EFFECT_NUM = 2;
	public static final int NUM_PARAMS = 4;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("TIME LEFT", true, true, "ms"),
		new ParamData("TIME RIGHT", true, true, "ms"),
		new ParamData("FEEDBACK", false, false, ""),
		new ParamData("WET", false, false, "")
	};

	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	public float rightChannelLevelMemory;
	public boolean rightChannelBeatSyncMemory;
	
	public Delay(BaseTrack track, int position) {
		super(track, position);
		rightChannelLevelMemory = -1;
		rightChannelBeatSyncMemory = true;
		paramsLinked = true;
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
		// last effect param for delay sets linked natively: 1 is true, 0 is false
		setEffectParam(track.getId(), position, NUM_PARAMS, linked ? 1 : 0);
	}
}
