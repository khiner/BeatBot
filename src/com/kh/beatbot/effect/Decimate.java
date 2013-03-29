package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Decimate extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.decimate);
	public static final int EFFECT_NUM = 1;
	public static final int NUM_PARAMS = 2;
	
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
	
	@Override
	protected void initParams() {
		if (params.isEmpty()) {
			params.add(new Param("RATE", true, false, "Hz"));
			params.add(new Param("BITS", true, false, "Bits"));
		}
	}
}
