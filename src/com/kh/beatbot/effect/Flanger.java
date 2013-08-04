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
		params.add(new EffectParam("Time", "ms", true, false));
		params.add(new EffectParam("Feedback", "", false, false));
		params.add(new EffectParam("Wet", "", false, false));
		params.add(new EffectParam("Mod Rate", "Hz", true, true));
		params.add(new EffectParam("Mod Amt", "", false, false));
		params.add(new EffectParam("Phase", "", false, false));
	}
}
