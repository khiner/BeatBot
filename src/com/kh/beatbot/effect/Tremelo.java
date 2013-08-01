package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;


public class Tremelo extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.tremelo);
	public static final int EFFECT_NUM = 6, NUM_PARAMS = 3;

	public Tremelo(BaseTrack track) {
		super(track);
	}
	
	public Tremelo(BaseTrack track, int position) {
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
		params.add(new Param("Rate", true, true, "Hz"));
		params.add(new Param("Phase", false, false, ""));
		params.add(new Param("Depth", false, false, ""));
	}
}
