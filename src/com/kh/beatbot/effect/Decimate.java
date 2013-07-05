package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.PlaybackManager;


public class Decimate extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.decimate);
	public static final int EFFECT_NUM = 1;
	public static final int NUM_PARAMS = 2;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("RATE", true, false, 0, PlaybackManager.SAMPLE_RATE, 8, "Hz"),
		new ParamData("BITS", true, false, 4, 28, 32, "Bits") // bits in range [4, 32]
	};
	
	static {
		PARAMS_DATA[0].hz = false;  // params do automatic scaling on hz params that we don't want.
	}
	
	public Decimate(BaseTrack track, int position) {
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

	public ParamData[] getParamsData() {
		return PARAMS_DATA;
	}
}
