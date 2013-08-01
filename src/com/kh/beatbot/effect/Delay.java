package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;

public class Delay extends Effect {
	
	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.delay);
	public static final int EFFECT_NUM = 2, NUM_PARAMS = 4;

	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	public float rightChannelLevelMemory;
	public boolean rightChannelBeatSyncMemory;
	
	public Delay(BaseTrack track) {
		super(track);
	}
	
	public Delay(BaseTrack track, int position) {
		super(track, position);
		rightChannelLevelMemory = -1;
		rightChannelBeatSyncMemory = true;
		paramsLinked = true;
	}

	public int getNum() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}
	
	@Override
	public void setParamsLinked(boolean linked) {
		super.setParamsLinked(linked);
		// last effect param for delay sets linked natively: 1 is true, 0 is false
		setEffectParam(track.getId(), position, NUM_PARAMS, linked ? 1 : 0);
	}
	
	@Override
	protected void initParams() {
		params.add(new Param("Time Left", true, true, "ms"));
		params.add(new Param("Time Right", true, true, "ms"));
		params.add(new Param("Feedback", false, false, ""));
		params.add(new Param("Wet", false, false, ""));
	}
}
