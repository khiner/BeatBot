package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FilterActivity extends EffectActivity {
	private int mode = 0;
	private ToggleButton[] filterButtons = new ToggleButton[3];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 2;
		setContentView(R.layout.filter_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.filterOn[trackNum]);
		filterButtons[0] = (ToggleButton)findViewById(R.id.lp_toggle);
		filterButtons[1] = (ToggleButton)findViewById(R.id.bp_toggle);
		filterButtons[2] = (ToggleButton)findViewById(R.id.hp_toggle);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.filterOn[trackNum] = on;
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

	public native void setFilterOn(int trackNum, boolean on, int mode);
	public native void setFilterMode(int trackNum, int mode);	
	public native void setFilterParam(int trackNum, int paramNum, float param);

	@Override
	public float getParamLevel(int paramNum) {
		switch(paramNum) {
		case 0:  return GlobalVars.filterX[trackNum];
		case 1:  return GlobalVars.filterY[trackNum];
		default: return 0;
		}
	}

	@Override
	public void setParamLevel(int paramNum, float level) {
		super.setParamLevel(paramNum, level);
		switch(paramNum) {
		case 0: GlobalVars.filterX[trackNum] = level;
			break;
		case 1: GlobalVars.filterY[trackNum] = level;
			break;
		default:
			return;
		}
		setFilterParam(trackNum, paramNum, level);
	}
	
	@Override
	public String getParamSuffix(int paramNum) {
		switch(paramNum) {
		case 0:
			return "Hz"; // frequency in Hertz
		case 1:
			return ""; // naked units for resonance
		default:
			return "";
		}
	}
}
