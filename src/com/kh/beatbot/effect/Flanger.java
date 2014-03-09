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
		params.add(new EffectParam(0, "Time", "ms", true, false));
		params.add(new EffectParam(1, "Feedback", "", false, false));
		params.add(new EffectParam(2, "Wet", "", false, false));
		params.add(new EffectParam(3, "Mod Rate", "Hz", true, true));
		params.add(new EffectParam(4, "Mod Amt", "", false, false));
		params.add(new EffectParam(5, "Phase", "", false, false));
	}
}
