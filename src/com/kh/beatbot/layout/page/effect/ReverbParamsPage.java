package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.view.TouchableSurfaceView;


public class ReverbParamsPage extends EffectParamsPage {

	public ReverbParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected String getName() {
		return Reverb.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Reverb.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Reverb.PARAMS_DATA;
	}
}
