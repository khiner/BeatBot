package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;

public class Chorus extends Effect {
	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.chorus);
	public static final int EFFECT_NUM = 0, NUM_PARAMS = 5;

	public Chorus() {
		super();
	}

	public Chorus(BaseTrack track, int position) {
		super(track, position);
	}

	public int getNum() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}

	@Override
	protected void initParams() {
		params.add(new Param(0, "Mod Rate").withUnits("Hz").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(1, "Mod Amt").withLevel(0.5f));
		params.add(new Param(2, "Time").withUnits("ms").logScale().withLevel(0.5f));
		params.add(new Param(3, "Feedback").withLevel(0.5f));
		params.add(new Param(4, "Wet").withLevel(0.5f));
	}
}
