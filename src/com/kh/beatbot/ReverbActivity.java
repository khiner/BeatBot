package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverb_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.reverbOn[trackNum]);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.reverbOn[trackNum] = on;
		setReverbOn(trackNum, on);
	}
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbParam(int trackNum, int paramNum, float feedback);

	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}

	@Override
	public float getParamLevel(int paramNum) {
		switch(paramNum) {
		case 0: return GlobalVars.reverbX[trackNum];
		case 1: return GlobalVars.reverbY[trackNum];
		default: return 0;
		}

	}

	@Override
	public void setParamLevel(int paramNum, float level) {
		super.setParamLevel(paramNum, level);
		switch(paramNum) {
		case 0: GlobalVars.reverbX[trackNum] = level;
			break;
		case 1: GlobalVars.reverbY[trackNum] = level;
			break;
		default: return;
		}
		setReverbParam(trackNum, paramNum, level);
	}
	
	@Override
	public String getParamSuffix(int paramNum) {
		switch(paramNum) {
		case 0: return ""; // naked units
		case 1: return ""; // naked units
		default: return "";
		}
	}
}
