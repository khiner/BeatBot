package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;


public class Reverb extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.reverb);
	public static final int EFFECT_NUM = 5;
	public static final int NUM_PARAMS = 2;
	
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("HF DAMP", false, false, ""),
		new ParamData("FEEDBACK", false, false, "")
	};
	
	public Reverb(BaseTrack track, int position) {
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
