package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.ParamData;

public class FlangerParamsPage extends EffectParamsPage {

	@Override
	protected int getNumParams() {
		return Flanger.NUM_PARAMS;
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Flanger.PARAMS_DATA;
	}
}
