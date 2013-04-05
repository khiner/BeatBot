package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.view.TouchableSurfaceView;

public class FlangerParamsPage extends EffectParamsPage {

	public FlangerParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected String getName() {
		return Flanger.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Flanger.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Flanger.PARAMS_DATA;
	}
}
