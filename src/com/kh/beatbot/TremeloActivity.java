package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class TremeloActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.tremeloOn[trackNum]);
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
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
	
	@Override
	public String getParamLabel(int paramNum) {
		if (paramNum < NUM_PARAMS)
			return getResources().getStringArray(R.array.tremelo_params)[paramNum];
		return ""; 
	}
}
