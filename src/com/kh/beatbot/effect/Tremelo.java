package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.GlobalVars;
import com.kh.beatbot.R;


public class Tremelo extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.tremelo);
	public static final int EFFECT_NUM = 6;
	public static final int NUM_PARAMS = 3;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("RATE", true, true, "Hz"),
		new ParamData("PHASE", false, false, ""),
		new ParamData("DEPTH", false, false, "")
	};
	
	public Tremelo(BaseTrack track, int position) {
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
	
	@Override
	public ParamData[] getParamsData() {
		return PARAMS_DATA;
	}
}
