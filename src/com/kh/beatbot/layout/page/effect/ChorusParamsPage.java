package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.ParamData;


public class ChorusParamsPage extends EffectParamsPage {
	
	@Override
	protected String getName() {
		return Chorus.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Chorus.NUM_PARAMS;
	}

	@Override
	protected ParamData[] getParamsData() {
		return Chorus.PARAMS_DATA;
	}
}
