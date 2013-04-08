package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.effect.ParamData;


public class DecimateParamsPage extends EffectParamsPage {

	@Override
	protected String getName() {
		return Decimate.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Decimate.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Decimate.PARAMS_DATA;
	}
}
