package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;


public class Reverb extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.reverb);
	public static final int EFFECT_NUM = 5, NUM_PARAMS = 2;
	
	public Reverb(BaseTrack track) {
		super(track);
	}
	
	public Reverb(BaseTrack track, int position) {
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
		params.add(new EffectParam(0, "HF DAMP", "", false, false));
		params.add(new EffectParam(1, "FEEDBACK", "", false, false));
	}
}
