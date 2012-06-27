package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;

public class DelayActivity extends EffectActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delay_layout);
		initLevelBars();
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.delayOn[trackNum]);		
	}
	
	public float getXValue() {
		return GlobalVars.delayX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.delayY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.delayX[trackNum] = xValue;
		setDelayTime(trackNum, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.delayY[trackNum] = yValue;		
		setDelayFeedback(trackNum, yValue);
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.delayOn[trackNum] = on;
		setDelayOn(trackNum, on);
	}
	
	public void setEffectDynamic(boolean dynamic) {
		return; // delay is always dynamic
	}
	
	public void beatMatch(View view) {
		setDelayBeatmatch(trackNum, ((ToggleButton)view).isChecked());
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayBeatmatch(int trackNum, boolean beatmatch);	
	public native void setDelayTime(int trackNum, float delay);
	public native void setDelayFeedback(int trackNum, float feedback);
}
