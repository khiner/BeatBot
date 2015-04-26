package com.kh.beatbot.effect;

public class Tremolo extends Effect {
	public static final String NAME = "Tremolo";
	public static final int EFFECT_NUM = 6, NUM_PARAMS = 3;

	public Tremolo() {
		super();
	}

	public Tremolo(int trackId, int position) {
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
		params.add(new Param(0, "Rate").withUnits("Hz").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(1, "Phase").withLevel(0.5f));
		params.add(new Param(2, "Depth").withLevel(0.5f));
	}
}
