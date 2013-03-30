package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Decimate extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.decimate);
	public static final int EFFECT_NUM = 1;
	public static final int NUM_PARAMS = 2;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("RATE", true, false, "Hz"),
		new ParamData("BITS", true, false, "Bits")
	};
	
	public Decimate(int trackNum, int position) {
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
