package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.view.TouchableSurfaceView;


public class DecimateParamsPage extends EffectParamsPage {

	public DecimateParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected int getNumParams() {
		return Decimate.NUM_PARAMS;
	}

	@Override
	protected void loadIcons() {
		toggleButton.setIconSource(new BBIconSource(R.drawable.bitcrush_label_off, R.drawable.bitcrush_label_on));
	}
}
