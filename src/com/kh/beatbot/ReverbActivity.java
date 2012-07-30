package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverb_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.reverbOn[trackNum]);
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
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbParam(int trackNum, int paramNum, float feedback);

	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
}
