package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.view.TronSeekbar2d;

public class ChorusActivity extends EffectActivity {
	private static final int NUM_PARAMS = 5;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chorus_layout);
		initParams(NUM_PARAMS);
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.chorusOn[trackNum]);
	}
	
	public float getXValue() {
		return GlobalVars.chorusX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.chorusY[trackNum];
	}
	
	public void setXValue(float xValue) {
		GlobalVars.chorusX[trackNum] = xValue;
		// exponential scale for rate
		setChorusParam(trackNum, 0, scaleLevel(xValue));
	}
	
	public void setYValue(float yValue) {
		GlobalVars.chorusY[trackNum] = yValue;		
		setChorusParam(trackNum, 1, yValue);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.chorusOn[trackNum] = on;
		setChorusOn(trackNum, on);
	}
	
	@Override
	public void setLevel(LevelListenable levelBar, float level) {		
		super.setLevel(levelBar, level);
		if (levelBar instanceof TronSeekbar2d)
			return;
		switch (levelBar.getId()) {
		case 2:
			GlobalVars.chorusWet[trackNum] = level;
			break;
		case 3:
			GlobalVars.chorusModRate[trackNum] = level;
			break;
		case 4:
			GlobalVars.chorusModAmt[trackNum] = level;
			break;
		}
		setChorusParam(trackNum, levelBar.getId(), scaleLevel(level));
	}
	
	public float getLevel(int paramNum) {
		switch (paramNum) {
		case 2:
			return GlobalVars.chorusWet[trackNum]; 
		case 3:
			return GlobalVars.chorusModRate[trackNum];
		case 4:
			return GlobalVars.chorusModAmt[trackNum];
		default:
			return 0;
		}
	}
			
	@Override
	public void notifyInit(LevelListenable levelBar) {
		super.notifyInit(levelBar);
		if (levelBar instanceof TronSeekbar2d)
			return;
		levelBar.setLevel(getLevel(levelBar.getId()));
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	public native void setChorusOn(int trackNum, boolean on);
	public native void setChorusParam(int trackNum, int paramNum, float param);
}
