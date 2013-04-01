package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.view.TouchableSurfaceView;


public class ReverbParamsPage extends EffectParamsPage {

	public ReverbParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected int getNumParams() {
		return Reverb.NUM_PARAMS;
	}
	
	@Override
	protected void loadIcons() {
		toggleButton.setIconSource(new ImageIconSource(R.drawable.reverb_label_off, R.drawable.reverb_label_on));
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Reverb.PARAMS_DATA;
	}
}
