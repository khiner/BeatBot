package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Reverb extends Effect {

	public Reverb(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	public void initParams() {
		numParams = 2;
		effectNum = 5;
		if (params.isEmpty()) {
			params.add(new Param("HF DAMP", false, false, ""));
			params.add(new Param("FEEDBACK", false, false, ""));
		}
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.reverb_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.reverb_label_off;
	}
}
