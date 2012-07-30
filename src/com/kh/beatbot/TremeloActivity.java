package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class TremeloActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tremelo_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.tremeloOn[trackNum]);
	}
	
	public float getXValue() {
		return GlobalVars.tremeloX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.tremeloY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.tremeloX[trackNum] = xValue;
		// exponential scale for mod rate
		setTremeloParam(trackNum, 0, scaleLevel(xValue));
	}
	
	public void setYValue(float yValue) {
		GlobalVars.tremeloY[trackNum] = yValue;
		setTremeloParam(trackNum, 1, yValue);
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.tremeloOn[trackNum] = on;
		setTremeloOn(trackNum, on);
	}
	
	public native void setTremeloOn(int trackNum, boolean on);
	public native void setTremeloParam(int trackNum, int paramNum, float param);

	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
}
