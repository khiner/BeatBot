package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.PlaybackManager;

public class Filter extends Effect {
	public static final String NAME = GlobalVars.mainActivity.getString(R.string.filter);
	public static final int EFFECT_NUM = 3;
	public static final int NUM_PARAMS = 4;
	
	private int mode = 0;
	
	public Filter(int trackNum, int position) {
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
	public void initParams() {
		if (params.isEmpty()) {
			params.add(new Param("FREQ", true, false, "Hz"));
			getParam(0).scale = PlaybackManager.SAMPLE_RATE / 2;
			params.add(new Param("RES", false, false, ""));
			params.add(new Param("MOD RATE", true, true, "Hz"));
			getParam(2).hz = true;
			params.add(new Param("MOD AMT", false, false, ""));
		}
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		setEffectParam(trackNum, position, 4, mode);
	}
}
