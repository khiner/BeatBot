package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.effect.Tremelo;


public class TremeloParamsPage extends EffectParamsPage {

	@Override
	protected int getNumParams() {
		return Tremelo.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Tremelo.PARAMS_DATA;
	}
}
