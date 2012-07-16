package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public class FlangerActivity extends EffectActivity {
	private static final int NUM_PARAMS = 5;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.flangerOn[trackNum]);
		
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
	}
	
	public float getXValue() {
		return GlobalVars.flangerX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.flangerY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.flangerX[trackNum] = xValue;
		setFlangerParam(trackNum, 0, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.flangerY[trackNum] = yValue;		
		setFlangerParam(trackNum, 1, yValue);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.flangerOn[trackNum] = on;
		setFlangerOn(trackNum, on);
	}
	
	public void setEffectDynamic(boolean dynamic) {
		return; // flanger is always dynamic
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (levelBar.getTag().equals(2)) {
			GlobalVars.flangerWet[trackNum] = level;
		} else if (levelBar.getTag().equals(3)) {
			GlobalVars.flangerModRate[trackNum] = level;
		} else if (levelBar.getTag().equals(4)) {
			GlobalVars.flangerModAmt[trackNum] = level;
		}
		setFlangerParam(trackNum, (Integer)levelBar.getTag(), level);
	}
	
	public float getWetValue() {
		return GlobalVars.flangerWet[trackNum];
	}
	
	public float getModRate() {
		return GlobalVars.flangerModRate[trackNum];
	}
		
	public float getModAmt() {
		return GlobalVars.flangerModAmt[trackNum];
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
			return getResources().getStringArray(R.array.flanger_params)[paramNum];
		return ""; 
	}
	
	public native void setFlangerOn(int trackNum, boolean on);
	public native void setFlangerParam(int trackNum, int paramNum, float param);
}
