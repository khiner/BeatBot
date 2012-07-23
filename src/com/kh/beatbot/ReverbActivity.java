package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class ReverbActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.reverbOn[trackNum]);
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
	}
	
	public float getXValue() {
		return GlobalVars.reverbX[trackNum];
	}

	public float getYValue() {
		return GlobalVars.reverbY[trackNum];
	}

	public void setXValue(float xValue) {
		GlobalVars.reverbX[trackNum] = xValue;
		setReverbParam(trackNum, 0, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.reverbY[trackNum] = yValue;
		setReverbParam(trackNum, 1, yValue);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.reverbOn[trackNum] = on;
		setReverbOn(trackNum, on);
	}

	public void setEffectDynamic(boolean dynamic) {
		return;  // reverb can only be dynamic
	}
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbParam(int trackNum, int paramNum, float feedback);

	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	@Override
	public String getParamLabel(int paramNum) {
		if (paramNum < NUM_PARAMS)
			return getResources().getStringArray(R.array.reverb_params)[paramNum];
		return ""; 
	}
}
