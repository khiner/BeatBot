package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;


public class Flanger extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.flanger);
	public static final int EFFECT_NUM = 4;
	public static final int NUM_PARAMS = 6;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("Time", true, false, "ms"),
		new ParamData("Feedback", false, false, ""),
		new ParamData("Wet", false, false, ""),
		new ParamData("Mod Rate", true, true, "Hz"),
		new ParamData("Mod Amt", false, false, ""),
		new ParamData("Phase", false, false, "")
	};
	
	public Flanger(BaseTrack track, int position) {
		super(track, position);
	}

	public String getName() {
		return NAME;
	}
	
	public int getNum() {
		return EFFECT_NUM;
	}

	public int numParams() {
		return NUM_PARAMS;
	}
	
	public ParamData[] getParamsData() {
		return PARAMS_DATA;
	}
}
