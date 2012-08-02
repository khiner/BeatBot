package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DelayActivity extends EffectActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 3;
		setContentView(R.layout.delay_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.delayOn[trackNum]);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.delayOn[trackNum] = on;
		setDelayOn(trackNum, on);
	}
	
	public void beatMatch(View view) {
		boolean beatmatch = ((ToggleButton)view).isChecked();
		GlobalVars.delayBeatmatch[trackNum] = beatmatch;
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayParam(int trackNum, int paramNum, float param);

	@Override
	public float getParamLevel(int paramNum) {
		switch (paramNum) {
		case 0:
			return GlobalVars.delayX[trackNum];
		case 1:
			return GlobalVars.delayY[trackNum];
		default:
			return 0;
		}
	}

	@Override
	public void setParamLevel(int paramNum, float level) {
		super.setParamLevel(paramNum, level);
		switch (paramNum) {
		case 0: GlobalVars.delayX[trackNum] = level;
			break;
		case 1: GlobalVars.delayY[trackNum] = level;
			break;
		default:
			return;
		}
		setDelayParam(trackNum, paramNum, level);
	}
	@Override
	public String getParamSuffix(int paramNum) {
		switch (paramNum) {
		case 0:
			return "ms"; // time in ms
		case 1:
			return ""; // naked units for feedback
		default:
			return "";
		}
	}
}
