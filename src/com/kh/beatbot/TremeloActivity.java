package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class TremeloActivity extends EffectActivity {
	private static final int NUM_PARAMS = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tremelo_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.tremeloOn[trackNum]);
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.tremeloOn[trackNum] = on;
		setTremeloOn(trackNum, on);
	}
	
	public native void setTremeloOn(int trackNum, boolean on);
	public native void setTremeloParam(int trackNum, int paramNum, float param);

	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}

	@Override
	public float getParamLevel(int paramNum) {
		switch(paramNum) {
		case 0 : return GlobalVars.tremeloX[trackNum];
		case 1 : return GlobalVars.tremeloY[trackNum];
		default : return 0;
		}
	}

	@Override
	public void setParamLevel(int paramNum, float level) {
		super.setParamLevel(paramNum, level);
		switch(paramNum) {
		case 0 : GlobalVars.tremeloX[trackNum] = level;
			break;
		case 1: GlobalVars.tremeloY[trackNum] = level;
			break;
		default :
			return;
		}
		setTremeloParam(trackNum, paramNum, level);
	}
	
	@Override
	public String getParamSuffix(int paramNum) {
		switch(paramNum) {
		case 0 : return "Hz"; // herrz for rate
		case 1: return ""; // naked units for depth
		default : return "";
		}
	}
	
}
