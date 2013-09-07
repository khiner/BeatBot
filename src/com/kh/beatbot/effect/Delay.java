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
	public float rightChannelLevelMemory = -1;
	public boolean rightChannelBeatSyncMemory = true;
	
	public Delay(BaseTrack track) {
		super(track);
	}
	
	public Delay(BaseTrack track, int position) {
		super(track, position);
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamIndex = 0;
		yParamIndex = 2;
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
		// y = feedback when linked / right delay time when not linked
		yParamIndex = linked ? 2 : 1;
	}
	
	@Override
	protected void initParams() {
		params.add(new EffectParam(0, "Left", "ms", true, true));
		params.add(new EffectParam(1, "Right", "ms", true, true));
		params.add(new EffectParam(2, "Feedback", "", false, false));
		params.add(new EffectParam(3, "Wet", "", false, false));
	}
}
