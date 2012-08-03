package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FilterActivity extends EffectActivity {
	private int mode = 0;
	private ToggleButton[] filterButtons = new ToggleButton[3];
	
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM] == null) {
			List<EffectParam> params = new ArrayList<EffectParam>();
			params.add(new EffectParam(true, 'x', "Hz"));
			params.add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM] = params;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EFFECT_NUM = 3;
		NUM_PARAMS = 2;
		setContentView(R.layout.filter_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
		filterButtons[0] = (ToggleButton)findViewById(R.id.lp_toggle);
		filterButtons[1] = (ToggleButton)findViewById(R.id.bp_toggle);
		filterButtons[2] = (ToggleButton)findViewById(R.id.hp_toggle);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setFilterOn(trackNum, on, mode);
	}
	
	public void selectFilterMode(View view) {
		for (int i = 0; i < filterButtons.length; i++) {
			if (view.equals(filterButtons[i])) {
				mode = i;
				setFilterMode(trackNum, mode);
			}
			else
				filterButtons[i].setChecked(false);
		}
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setFilterParam(trackNum, paramNum, level);
	}
	
	public native void setFilterOn(int trackNum, boolean on, int mode);
	public native void setFilterMode(int trackNum, int mode);	
	public native void setFilterParam(int trackNum, int paramNum, float param);
}
