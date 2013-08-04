package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.PlaybackManager;

public class Decimate extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity
			.getString(R.string.decimate);
	public static final int EFFECT_NUM = 1, NUM_PARAMS = 2;

	public Decimate(BaseTrack track) {
		super(track);
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
		params.add(new EffectParam("Rate", "Hz", 0,
				PlaybackManager.SAMPLE_RATE, 8, true, false));
		// bits in range [4, 32]
		params.add(new EffectParam("Bits", "Bits", 4, 28, 32, true, false));
		// params do automatic scaling on hz params that we don't want.
		params.get(0).hz = false;
	}
}
