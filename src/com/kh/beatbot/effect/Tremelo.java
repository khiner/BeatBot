package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Tremelo extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.tremelo);
	public static final int EFFECT_NUM = 6;
	public static final int NUM_PARAMS = 3;
	
	public Tremelo(int trackNum, int position) {
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
			params.add(new Param("RATE", true, true, "Hz"));
			params.add(new Param("PHASE", false, false, ""));
			getParam(0).hz = true;
			params.add(new Param("DEPTH", false, false, ""));
		}
	}
}
