package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;


public class Tremolo extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.tremolo);
	public static final int EFFECT_NUM = 6, NUM_PARAMS = 3;

	public Tremolo(BaseTrack track) {
		super(track);
	}
	
	public Tremolo(BaseTrack track, int position) {
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
		params.add(new EffectParam(0, "Rate", "Hz", true, true));
		params.add(new EffectParam(1, "Phase", "", false, false));
		params.add(new EffectParam(2, "Depth", "", false, false));
	}
}
