package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.view.TronSeekbar2d;

public class DelayActivity extends EffectActivity {
	private final int NUM_PARAMS = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delay_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.delayOn[trackNum]);
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
	public void setLevel(LevelListenable levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (!(levelBar instanceof TronSeekbar2d) && 
				levelBar.getId() == 2) {
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
	public void notifyInit(LevelListenable levelBar) {
		super.notifyInit(levelBar); 
		if (!(levelBar instanceof TronSeekbar2d) && 
				levelBar.getId() == 2)
			levelBar.setLevel(getWetValue());
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayParam(int trackNum, int paramNum, float param);
}
