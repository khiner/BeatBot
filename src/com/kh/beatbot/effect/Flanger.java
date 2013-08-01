package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;


public class Flanger extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.flanger);
	public static final int EFFECT_NUM = 4, NUM_PARAMS = 6;
	
	public Flanger(BaseTrack track) {
		super(track);
	}
	
	public Flanger(BaseTrack track, int position) {
		super(track, position);
	}

	public String getName() {
		return NAME;
	}
	
	public int getNum() {
		return EFFECT_NUM;
	}

	@Override
	protected void initParams() {
		params.add(new Param("Time", true, false, "ms"));
		params.add(new Param("Feedback", false, false, ""));
		params.add(new Param("Wet", false, false, ""));
		params.add(new Param("Mod Rate", true, true, "Hz"));
		params.add(new Param("Mod Amt", false, false, ""));
		params.add(new Param("Phase", false, false, ""));
	}
}
