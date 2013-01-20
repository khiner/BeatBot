package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Chorus extends Effect {

	public Chorus(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	protected void initParams() {
		effectNum = 0;
		numParams = 5;
		if (params.isEmpty()) {
			params.add(new Param("MOD RATE", true, true, "Hz"));
			getParam(0).hz = true;
			params.add(new Param("MOD AMT", false, false, ""));
			params.add(new Param("TIME", false, false, ""));
			params.add(new Param("FEEDBACK", true, true, "ms"));
			params.add(new Param("WET", false, false, ""));
		}
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.chorus_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.chorus_label_off;
	}
}
