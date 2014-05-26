package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.PlaybackManager;

public class Decimate extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.decimate);
	public static final int EFFECT_NUM = 1, NUM_PARAMS = 2;

	public Decimate() {
		super();
	}

	public Decimate(BaseTrack track, int position) {
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
		params.add(new Param(0, "Rate").scale(PlaybackManager.SAMPLE_RATE).withUnits("Hz")
				.logScale().withLevel(0.5f));
		// bits in range [4, 32]
		params.add(new Param(1, "Bits").add(4).scale(28).withUnits("Bits").logScale(32)
				.withLevel(0.5f));
		// params do automatic scaling on hz params that we don't want.
		params.get(0).hz = false;
	}
}
