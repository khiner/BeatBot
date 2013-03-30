package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Chorus extends Effect {
	public static final String NAME = GlobalVars.mainActivity.getString(R.string.chorus);
	public static final int EFFECT_NUM = 0;
	public static final int NUM_PARAMS = 5;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("MOD RATE", true, true, "Hz"),
		new ParamData("MOD AMT", false, false, ""),
		new ParamData("TIME", false, false, ""),
		new ParamData("FEEDBACK", true, true, "ms"),
		new ParamData("WET", false, false, "")
	};
	
	public Chorus(int trackNum, int position) {
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
}
