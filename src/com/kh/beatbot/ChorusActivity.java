package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public class ChorusActivity extends EffectActivity {
	private static final int NUM_PARAMS = 5;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.chorusOn[trackNum]);
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
	}
	
	public float getXValue() {
		return GlobalVars.chorusX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.chorusY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.chorusX[trackNum] = xValue;
		setChorusParam(trackNum, 0, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.chorusY[trackNum] = yValue;		
		setChorusParam(trackNum, 1, yValue);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.chorusOn[trackNum] = on;
		setChorusOn(trackNum, on);
	}
	
	public void setEffectDynamic(boolean dynamic) {
		return; // chorus is always dynamic
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (levelBar.getTag().equals(2)) {
			GlobalVars.chorusWet[trackNum] = level;
		} else if (levelBar.getTag().equals(3)) {
			GlobalVars.chorusModRate[trackNum] = level;
		} else if (levelBar.getTag().equals(4)) {
			GlobalVars.chorusModAmt[trackNum] = level;
		}
		setChorusParam(trackNum, (Integer)levelBar.getTag(), level);
	}
	
	public float getWetValue() {
		return GlobalVars.chorusWet[trackNum];
	}
	
	public float getModRate() {
		return GlobalVars.chorusModRate[trackNum];
	}
		
	public float getModAmt() {
		return GlobalVars.chorusModAmt[trackNum];
	}
	
	@Override
	public void notifyInit(TronSeekbar levelBar) {
		super.notifyInit(levelBar);
		if (levelBar.getTag().equals(2))
			levelBar.setLevel(getWetValue());
		else if (levelBar.getTag().equals(3)) {
			levelBar.setLevel(getModRate());
		} else if (levelBar.getTag().equals(4)) {
			levelBar.setLevel(getModAmt());
		}
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	@Override
	public String getParamLabel(int paramNum) {
		if (paramNum < NUM_PARAMS)
			return getResources().getStringArray(R.array.chorus_params)[paramNum];
		return ""; 
	}
	
	public native void setChorusOn(int trackNum, boolean on);
	public native void setChorusParam(int trackNum, int paramNum, float param);
}
