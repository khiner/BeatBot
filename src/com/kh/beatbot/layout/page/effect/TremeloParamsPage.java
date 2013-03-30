package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.effect.Tremelo;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.view.TouchableSurfaceView;


public class TremeloParamsPage extends EffectParamsPage {

	public TremeloParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected int getNumParams() {
		return Tremelo.NUM_PARAMS;
	}

	@Override
	protected void loadIcons() {
		toggleButton.setIconSource(new BBIconSource( R.drawable.tremelo_label_off, R.drawable.tremelo_label_on));
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Tremelo.PARAMS_DATA;
	}
}
