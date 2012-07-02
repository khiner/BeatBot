package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public class DelayActivity extends EffectActivity {	
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
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (levelBar.getTag().equals(2)) {
			setWetValue(level);
		}
	}
		
	public void setWetValue(float wet) {
		GlobalVars.delayWet[trackNum] = wet;
		setDelayWet(trackNum, wet);
	}
	
	public void beatMatch(View view) {
		setDelayBeatmatch(trackNum, ((ToggleButton)view).isChecked());
	}
	
	@Override
	public int getNumParams() {
		return 3;
	}
	
	@Override
	public String getLabelX() {
		return getResources().getString(R.string.delay);
	}

	@Override
	public String getLabelY() {
		return getResources().getString(R.string.feedback);
	}

	@Override
	public String getLabelParam3() {
		return getResources().getString(R.string.wet);
	}	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayBeatmatch(int trackNum, boolean beatmatch);	
	public native void setDelayTime(int trackNum, float delay);
	public native void setDelayFeedback(int trackNum, float feedback);
	public native void setDelayWet(int trackNum, float wet);
}
