package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	final int NUM_PARAMS = 2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decimate_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.decimateOn[trackNum]);
	}
	
	public float getXValue() {
		return GlobalVars.decimateX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.decimateY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.decimateX[trackNum] = xValue;
		// exponential scale for rate
		setDecimateParam(trackNum, 0, scaleLevel(xValue));
	}
	
	public void setYValue(float yValue) {
		GlobalVars.decimateY[trackNum] = yValue;
		// exponential scale for bits
		setDecimateParam(trackNum, 1, scaleLevel(yValue));
	}
	
	public boolean isEffectOn() {
		return GlobalVars.decimateOn[trackNum];
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.decimateOn[trackNum] = on;		
		setDecimateOn(trackNum, on);
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
