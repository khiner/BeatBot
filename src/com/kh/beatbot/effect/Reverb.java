package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Reverb extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.reverb);
	public static final int EFFECT_NUM = 5;
	public static final int NUM_PARAMS = 2;
	
	public Reverb(int trackNum, int position) {
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
			params.add(new Param("HF DAMP", false, false, ""));
			params.add(new Param("FEEDBACK", false, false, ""));
		}
	}
}
