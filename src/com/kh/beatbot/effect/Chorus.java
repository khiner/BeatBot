package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;

public class Chorus extends Effect {
	public static final String NAME = BeatBotActivity.mainActivity
			.getString(R.string.chorus);
	public static final int EFFECT_NUM = 0, NUM_PARAMS = 5;

	public Chorus(BaseTrack track) {
		super(track);
	}
	
	public Chorus(BaseTrack track, int position) {
		super(track, position);
	}

	public int getNum() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}
	
	@Override
	protected void initParams() {
		params.add(new Param("Mod Rate", true, true, "Hz"));
		params.add(new Param("Mod Amt", false, false, ""));
		params.add(new Param("Time", true, false, "ms"));
		params.add(new Param("Feedback", false, false, ""));
		params.add(new Param("Wet", false, false, ""));
	}
}
