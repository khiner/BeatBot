package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;

public class Reverb extends Effect {
	public static final String NAME = "Reverb";
	public static final int EFFECT_NUM = 5, NUM_PARAMS = 8;

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
		params.add(new Param(0, "Reverb Vol").withLevel(0.5f));
		params.add(new Param(1, "Room Size").withLevel(0.5f));
		params.add(new Param(2, "Decay").withLevel(0.5f));
		params.add(new Param(3, "Density").withLevel(0.5f));
		params.add(new Param(4, "Pre-delay").withLevel(0.5f));
		params.add(new Param(5, "Early/Late Mix").withLevel(0.5f));
		params.add(new Param(6, "Damping").withLevel(0.5f));
		params.add(new Param(7, "Brightness").withLevel(0.5f));
	}
}
