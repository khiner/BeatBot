package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;

public class Flanger extends Effect {
	public static final String NAME = "Flanger";
	public static final int EFFECT_NUM = 4, NUM_PARAMS = 6;

	public Flanger() {
		super();
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
		params.add(new Param(0, "Time").withUnits("ms").logScale().withLevel(0.5f));
		params.add(new Param(1, "Feedback").withLevel(0.5f));
		params.add(new Param(2, "Wet").withLevel(0.5f));
		params.add(new Param(3, "Mod Rate").withUnits("Hz").logScale().beatSyncable()
				.withLevel(0.5f));
		params.add(new Param(4, "Mod Amt").withLevel(0.5f));
		params.add(new Param(5, "Phase").withLevel(0.5f));
	}
}
