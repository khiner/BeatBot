package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.decimateOn[trackNum]);
	}
	
	public float getXValue() {
		return GlobalVars.decimateX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.decimateY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.decimateX[trackNum] = xValue;		
		setDecimateBits(trackNum, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.decimateY[trackNum] = yValue;
		setDecimateRate(trackNum, yValue);
	}
	
	public boolean isEffectOn() {
		return GlobalVars.decimateOn[trackNum];
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.decimateOn[trackNum] = on;
		
		setDecimateOn(trackNum, on);
	}
	
	public native void setDecimateOn(int trackNum, boolean on);	
	public native void setDecimateBits(int trackNum, float bits);
	public native void setDecimateRate(int trackNum, float rate);
}
