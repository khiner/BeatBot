package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.PlaybackManager;

public class Filter extends Effect {
	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.filter);
	public static final int EFFECT_NUM = 3, NUM_PARAMS = 4;
	
	private int mode = 0;
	
	public Filter(BaseTrack track) {
		super(track);
	}
	
	public Filter(BaseTrack track, int position) {
		super(track, position);
	}

	public int getNum() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}
	
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		setEffectParam(track.getId(), position, 4, mode);
	}
	
	@Override
	protected void initParams() {
		params.add(new Param("Freq", true, false, "Hz"));
		params.add(new Param("Res", false, false, ""));
		params.add(new Param("Mod Rate", true, true, "Hz"));
		params.add(new Param("Mod Amt", false, false, ""));
		params.get(0).scaleValue = PlaybackManager.SAMPLE_RATE / 2;
		params.get(0).hz = false;
	}
}
