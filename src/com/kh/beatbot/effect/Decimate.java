package com.kh.beatbot.effect;

import com.kh.beatbot.R;

public class Decimate extends Effect {

	public Decimate(String name, int trackNum, int position) {
		super(name, trackNum, position);
	}

	@Override
	protected void initParams() {
		numParams = 2;
		effectNum = 1;
		if (params.isEmpty()) {
			params.add(new Param("RATE", true, false, "Hz"));
			params.add(new Param("BITS", true, false, "Bits"));
		}
	}

	@Override
	public int getOnDrawableId() {
		return R.drawable.bitcrush_label_on;
	}

	@Override
	public int getOffDrawableId() {
		return R.drawable.bitcrush_label_off;
	}
}
