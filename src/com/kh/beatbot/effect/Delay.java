package com.kh.beatbot.effect;

import com.kh.beatbot.track.BaseTrack;

public class Delay extends Effect {
	public static final String NAME = "Delay";
	public static final int EFFECT_NUM = 2, NUM_PARAMS = 4;

	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	public float rightChannelLevelMemory = -1;
	public boolean rightChannelBeatSyncMemory = true;

	public Delay() {
		super();
	}

	public Delay(BaseTrack track, int position) {
		super(track, position);
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamIndex = 0;
		yParamIndex = 2;
		paramsLinked = true;
	}

	public int getId() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}

	@Override
	public void setParamsLinked(boolean linked) {
		super.setParamsLinked(linked);
		// y = feedback when linked / right delay time when not linked
		yParamIndex = linked ? 2 : 1;
	}

	@Override
	protected void initParams() {
		params.add(new Param(0, "Left").withUnits("ms").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(1, "Right").withUnits("ms").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(2, "Feedback").withLevel(0.5f));
		params.add(new Param(3, "Wet").withLevel(0.5f));
	}
}
