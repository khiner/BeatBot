package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.effect.Tremelo;
import com.kh.beatbot.view.TouchableSurfaceView;


public class TremeloParamsPage extends EffectParamsPage {

	public TremeloParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected String getName() {
		return Tremelo.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Tremelo.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Tremelo.PARAMS_DATA;
	}
}
