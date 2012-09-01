package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FilterActivity extends EffectActivity {
	private ToggleButton[] filterButtons = new ToggleButton[3];
	
	@Override
	public void initParams() {
		EFFECT_NUM = GlobalVars.FILTER_EFFECT_NUM;
		NUM_PARAMS = 4;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, false, "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "Hz"));
			getParam(2).hz = true;
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		filterButtons[0] = (ToggleButton)findViewById(R.id.lp_toggle);
		filterButtons[1] = (ToggleButton)findViewById(R.id.bp_toggle);
		filterButtons[2] = (ToggleButton)findViewById(R.id.hp_toggle);
		filterButtons[GlobalVars.filterMode[trackNum]].setChecked(true);
	}

	public void selectFilterMode(View view) {
		for (int i = 0; i < filterButtons.length; i++) {
			if (view.equals(filterButtons[i])) {
				GlobalVars.filterMode[trackNum] = i;
				setFilterOn(trackNum, GlobalVars.effectOn[EFFECT_NUM][trackNum], i);
				filterButtons[i].setChecked(true);
			}
			else
				filterButtons[i].setChecked(false);
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.filter_param_layout;
	}
	
	@Override
	public int getOnDrawableId() {
		return R.drawable.filter_label_on;
	}
	
	@Override
	public int getOffDrawableId() {
		return R.drawable.filter_label_off;
	}
}
