package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public class DelayActivity extends EffectActivity {
	private final int NUM_PARAMS = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delay_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.delayOn[trackNum]);
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
	}
	
	public float getXValue() {
		return GlobalVars.delayX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.delayY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.delayX[trackNum] = xValue;
		// exponential scale for time
		float scaledLevel = scaleLevel(xValue);
		if (GlobalVars.delayBeatmatch[trackNum])
			scaledLevel = quantizeToBeat(scaledLevel);
		setDelayParam(trackNum, 0, scaledLevel);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.delayY[trackNum] = yValue;		
		setDelayParam(trackNum, 1, yValue);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.delayOn[trackNum] = on;
		setDelayOn(trackNum, on);
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (levelBar.getTag().equals(2)) {
			setWetValue(level);
		}
	}
		
	public void setWetValue(float wet) {
		GlobalVars.delayWet[trackNum] = wet;
		setDelayParam(trackNum, 2, wet);
	}
	
	public float getWetValue() {
		return GlobalVars.delayWet[trackNum];
	}
	
	public void beatMatch(View view) {
		boolean beatmatch = ((ToggleButton)view).isChecked();
		GlobalVars.delayBeatmatch[trackNum] = beatmatch;
	}
	
	@Override
	public void notifyInit(TronSeekbar levelBar) {
		super.notifyInit(levelBar);
		if (levelBar.getTag().equals(2))
			levelBar.setLevel(getWetValue());
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	@Override
	public String getParamLabel(int paramNum) {
		if (paramNum < NUM_PARAMS)
			return getResources().getStringArray(R.array.delay_params)[paramNum];
		return ""; 
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayParam(int trackNum, int paramNum, float param);
}
