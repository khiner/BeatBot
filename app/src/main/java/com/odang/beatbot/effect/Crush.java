package com.odang.beatbot.effect;

import com.odang.beatbot.manager.PlaybackManager;

public class Crush extends Effect {

	public static final String NAME = "Crush";
	public static final int EFFECT_NUM = 1, NUM_PARAMS = 2;

	public Crush() {
		super();
	}

	public Crush(int trackId, int position) {
		super(trackId, position);
	}

	public String getName() {
		return NAME;
	}

	public int getId() {
		return EFFECT_NUM;
	}

	@Override
	protected void initParams() {
		params.add(new Param(0, "Rate").scale(PlaybackManager.SAMPLE_RATE).withUnits("Hz")
				.logScale().withLevel(0.5f));
		// bits in range [4, 32]
		params.add(new Param(1, "Bits").add(4).scale(28).withUnits("Bits").logScale(32)
				.withLevel(0.5f));
		// params do automatic scaling on hz params that we don't want.
		params.get(0).hz = false;
	}
}
