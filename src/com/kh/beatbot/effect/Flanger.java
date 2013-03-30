package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Flanger extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.flanger);
	public static final int EFFECT_NUM = 4;
	public static final int NUM_PARAMS = 6;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("TIME", true, false, "ms"),
		new ParamData("FEEDBACK", false, false, ""),
		new ParamData("WET", false, false, ""),
		new ParamData("MOD RATE", true, true, "Hz"),
		new ParamData("MOD AMT", false, false, ""),
		new ParamData("PHASE", false, false, "")
	};
	
	public Flanger(int trackNum, int position) {
		super(trackNum, position);
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
