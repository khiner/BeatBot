package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;

public class Reverb extends Effect {
	public static final String NAME = "Reverb";
	public static final int EFFECT_NUM = 5, NUM_PARAMS = 2;

	public Reverb() {
		super();
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
		params.add(new Param(0, "HF Damp").withLevel(0.5f));
		params.add(new Param(1, "Feedback").withLevel(0.5f));
	}
}
