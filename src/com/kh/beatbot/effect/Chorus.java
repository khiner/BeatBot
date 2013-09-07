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
		params.add(new EffectParam(0, "Mod Rate", "Hz", true, true));
		params.add(new EffectParam(1, "Mod Amt", "", false, false));
		params.add(new EffectParam(2, "Time", "ms", true, false));
		params.add(new EffectParam(3, "Feedback", "", false, false));
		params.add(new EffectParam(4, "Wet", "", false, false));
	}
}
