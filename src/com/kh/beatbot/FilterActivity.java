package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FilterActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	private int mode = 0;
	private ToggleButton[] filterButtons = new ToggleButton[3];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.filterOn[trackNum]);
		filterButtons[0] = (ToggleButton)findViewById(R.id.lp_toggle);
		filterButtons[1] = (ToggleButton)findViewById(R.id.bp_toggle);
		filterButtons[2] = (ToggleButton)findViewById(R.id.hp_toggle);
	}
	
	public float getXValue() {
		return GlobalVars.filterX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.filterY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.filterX[trackNum] = xValue;
		// exponential scale for freq
		setFilterParam(trackNum, 0, scaleLevel(xValue));
	}
	
	public void setYValue(float yValue) {
		GlobalVars.filterY[trackNum] = yValue;
		setFilterParam(trackNum, 1, yValue);
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
	public int getNumParams() {
		return NUM_PARAMS;
	}
}
