package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;


public class Chorus extends Effect {
	public static final String NAME = GlobalVars.mainActivity.getString(R.string.chorus);
	public static final int EFFECT_NUM = 0;
	public static final int NUM_PARAMS = 5;
	
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
	
	@Override
	protected void initParams() {
		if (params.isEmpty()) {
			params.add(new Param("MOD RATE", true, true, "Hz"));
			getParam(0).hz = true;
			params.add(new Param("MOD AMT", false, false, ""));
			params.add(new Param("TIME", false, false, ""));
			params.add(new Param("FEEDBACK", true, true, "ms"));
			params.add(new Param("WET", false, false, ""));
		}
	}
}
