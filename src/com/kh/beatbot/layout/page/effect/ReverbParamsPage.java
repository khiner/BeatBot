package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.effect.Reverb;


public class ReverbParamsPage extends EffectParamsPage {

	@Override
	protected int getNumParams() {
		return Reverb.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Reverb.PARAMS_DATA;
	}
}
