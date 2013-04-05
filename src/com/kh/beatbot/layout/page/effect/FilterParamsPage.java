package com.kh.beatbot.layout.page.effect;

import android.view.View;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.view.ToggleButton;
import com.kh.beatbot.view.TouchableSurfaceView;


public class FilterParamsPage extends EffectParamsPage {

	private ToggleButton[] filterToggles;
	
	public FilterParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected String getName() {
		return Filter.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Filter.NUM_PARAMS;
	}
	
	@Override
	public void init() {

	}

	@Override
	public void createChildren() {
		super.createChildren();
		filterToggles = new ToggleButton[3];
		for (int i = 0; i < filterToggles.length; i++) {
			filterToggles[i] = new ToggleButton((TouchableSurfaceView)root);
			addChild(filterToggles[i]);
		}
	}

	@Override
	protected void loadIcons() {
		filterToggles[0].setIconSource(new ImageIconSource(R.drawable.lowpass_filter_icon, R.drawable.lowpass_filter_icon_selected));
		filterToggles[1].setIconSource(new ImageIconSource(R.drawable.bandpass_filter_icon, R.drawable.bandpass_filter_icon_selected));
		filterToggles[2].setIconSource(new ImageIconSource(R.drawable.highpass_filter_icon, R.drawable.highpass_filter_icon_selected));
		filterToggles[0].setChecked(true);
	}
	
	public void selectFilterMode(View view) {
		for (int i = 0; i < filterToggles.length; i++) {
			if (view.equals(filterToggles[i])) {
				((Filter) effect).setMode(i);
				filterToggles[i].setChecked(true);
			} else
				filterToggles[i].setChecked(false);
		}
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Filter.PARAMS_DATA;
	}
}
