package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.view.TouchableSurfaceView;


public class ChorusParamsPage extends EffectParamsPage {

	public ChorusParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	@Override
	protected int getNumParams() {
		return Chorus.NUM_PARAMS;
	}

	@Override
	protected void loadIcons() {
		toggleButton.setIconSource(new BBIconSource(R.drawable.chorus_label_off, R.drawable.chorus_label_on));
	}

	@Override
	protected ParamData[] getParamsData() {
		return Chorus.PARAMS_DATA;
	}
}
