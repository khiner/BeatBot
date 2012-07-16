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
		setFlangerTime(trackNum, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.flangerY[trackNum] = yValue;		
		setFlangerFeedback(trackNum, yValue);
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
			setWetValue(level);
		} else if (levelBar.getTag().equals(3)) {
			setModRateValue(level);
		} else if (levelBar.getTag().equals(4)) {
			setModAmtValue(level);
		}
	}
		
	public void setWetValue(float wet) {
		GlobalVars.flangerWet[trackNum] = wet;
		setFlangerWet(trackNum, wet);
	}

	public void setModRateValue(float modRate) {
		GlobalVars.flangerModRate[trackNum] = modRate;
		setFlangerModRate(trackNum, modRate);
	}

	public void setModAmtValue(float modAmt) {
		GlobalVars.flangerModAmt[trackNum] = modAmt;
		setFlangerModAmt(trackNum, modAmt);
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
	public native void setFlangerTime(int trackNum, float delay);
	public native void setFlangerFeedback(int trackNum, float feedback);
	public native void setFlangerWet(int trackNum, float wet);
	public native void setFlangerModRate(int trackNum, float modRate);
	public native void setFlangerModAmt(int trackNum, float modAmt);
}
