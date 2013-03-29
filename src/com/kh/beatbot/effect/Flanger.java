package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Flanger extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.flanger);
	public static final int EFFECT_NUM = 4;
	public static final int NUM_PARAMS = 6;
	
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

	@Override
	public void initParams() {
		if (params.isEmpty()) {
			params.add(new Param("TIME", true, false, "ms"));
			params.add(new Param("FEEDBACK", false, false, ""));
			params.add(new Param("WET", false, false, ""));
			params.add(new Param("MOD RATE", true, true, "Hz"));
			getParam(3).hz = true;
			params.add(new Param("MOD AMT", false, false, ""));
			params.add(new Param("PHASE", false, false, ""));
		}
	}
}
