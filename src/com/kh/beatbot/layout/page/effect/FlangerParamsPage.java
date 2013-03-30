package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.view.TouchableSurfaceView;

public class FlangerParamsPage extends EffectParamsPage {

	public FlangerParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected int getNumParams() {
		return Flanger.NUM_PARAMS;
	}
	
	@Override
	protected void loadIcons() {
		toggleButton.setIconSource(new BBIconSource(R.drawable.flanger_label_on, R.drawable.flanger_label_off));
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Flanger.PARAMS_DATA;
	}
}
