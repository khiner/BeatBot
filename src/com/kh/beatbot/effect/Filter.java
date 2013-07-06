package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.PlaybackManager;

public class Filter extends Effect {
	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.filter);
	public static final int EFFECT_NUM = 3;
	public static final int NUM_PARAMS = 4;
	public static final ParamData[] PARAMS_DATA = {
		new ParamData("FREQ", true, false, "Hz"),
		new ParamData("RES", false, false, ""),
		new ParamData("MOD RATE", true, true, "Hz"),
		new ParamData("MOD AMT", false, false, "")
	};
	
	static {
		PARAMS_DATA[0].scaleValue = PlaybackManager.SAMPLE_RATE / 2;
		PARAMS_DATA[0].hz = false;
	}
	
	private int mode = 0;
	
	public Filter(BaseTrack track, int position) {
		super(track, position);
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
	
	public ParamData[] getParamsData() {
		return PARAMS_DATA;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		setEffectParam(track.getId(), position, 4, mode);
	}
}
