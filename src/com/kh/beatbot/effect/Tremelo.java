package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Tremelo extends Effect {

	public Tremelo(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	public void initParams() {
		numParams = 3;
		effectNum = 6;
		if (params.isEmpty()) {
			params.add(new Param("RATE", true, true, "Hz"));
			params.add(new Param("PHASE", false, false, ""));
			getParam(0).hz = true;
			params.add(new Param("DEPTH", false, false, ""));
		}
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.tremelo_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.tremelo_label_off;
	}
}
