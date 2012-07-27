package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class DecimateActivity extends EffectActivity {
	final int NUM_PARAMS = 2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.decimateOn[trackNum]);
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
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
	
	@Override
	public String getParamLabel(int paramNum) {
		if (paramNum < NUM_PARAMS)
			return getResources().getStringArray(R.array.decimate_params)[paramNum];
		return "";
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
