package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class FilterActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	private int mode = 0;
	private ToggleButton[] filterButtons = new ToggleButton[3];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.filterOn[trackNum]);
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
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
		setFilterParam(trackNum, 0, xValue);
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
	
	@Override
	public String getParamLabel(int paramNum) {
		if (paramNum < NUM_PARAMS)
			return getResources().getStringArray(R.array.filter_params)[paramNum];
		return ""; 
	}
}
