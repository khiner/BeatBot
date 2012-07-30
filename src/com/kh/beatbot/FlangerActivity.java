package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.view.TronSeekbar2d;

public class FlangerActivity extends EffectActivity {
	private static final int NUM_PARAMS = 6;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flanger_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.flangerOn[trackNum]);
	}
	
	public float getXValue() {
		return GlobalVars.flangerX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.flangerY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.flangerX[trackNum] = xValue;
		// exponential scale for base time
		setFlangerParam(trackNum, 0, scaleLevel(xValue));
	}
	
	public void setYValue(float yValue) {
		GlobalVars.flangerY[trackNum] = yValue;		
		setFlangerParam(trackNum, 1, yValue);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.flangerOn[trackNum] = on;
		setFlangerOn(trackNum, on);
	}
	
	@Override
	public void setLevel(LevelListenable levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (levelBar instanceof TronSeekbar2d)
			return;
		switch (levelBar.getId()) {
		case 2:
			GlobalVars.flangerWet[trackNum] = level;
			break;
		case 3:
			GlobalVars.flangerModRate[trackNum] = level;
			// exponential scale for mod rate
			level = scaleLevel(level);
			break;
		case 4:
			GlobalVars.flangerModAmt[trackNum] = level;
		}
		setFlangerParam(trackNum, levelBar.getId(), level);
	}
	
	public float getLevel(int paramNum) {
		switch (paramNum) {
		case 2:
			return GlobalVars.flangerWet[trackNum]; 
		case 3:
			return GlobalVars.flangerModRate[trackNum];
		case 4:
			return GlobalVars.flangerModAmt[trackNum];
		default:
			return 0;
		}
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
	public void notifyInit(LevelListenable levelBar) {
		if (levelBar instanceof TronSeekbar2d)
			return;
		super.notifyInit(levelBar);
		levelBar.setLevel(getLevel(levelBar.getId()));
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	public native void setFlangerOn(int trackNum, boolean on);
	public native void setFlangerParam(int trackNum, int paramNum, float param);
}
