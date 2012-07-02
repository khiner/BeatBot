package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class DecimateActivity extends EffectActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.decimateOn[trackNum]);
		((TronSeekbar2d)findViewById(R.id.xyParamBar)).addLevelListener(this);
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
	
	public void setEffectDynamic(boolean dynamic) {
		setDecimateDynamic(trackNum, dynamic);
	}
	
	public native void setDecimateOn(int trackNum, boolean on);	
	public native void setDecimateDynamic(int trackNum, boolean dynamic);	
	public native void setDecimateBits(int trackNum, float bits);
	public native void setDecimateRate(int trackNum, float rate);

	@Override
	public int getNumParams() {
		return 2;
	}
	
	@Override
	public String getLabelX() {
		return getResources().getString(R.string.bits);
	}

	@Override
	public String getLabelY() {
		return getResources().getString(R.string.samplerate);
	}

	@Override
	public String getLabelParam3() {
		return "";
	}
}
