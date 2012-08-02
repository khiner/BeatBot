package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	final int NUM_PARAMS = 2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decimate_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.decimateOn[trackNum]);
	}
		
	public boolean isEffectOn() {
		return GlobalVars.decimateOn[trackNum];
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.decimateOn[trackNum] = on;		
		setDecimateOn(trackNum, on);
	}
	
	@Override
	public int getNumParams() {
		return NUM_PARAMS;
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);

	@Override
	public float getParamLevel(int paramNum) {
		switch (paramNum) {
		case 0:
			return GlobalVars.decimateX[trackNum];
		case 1:
			return GlobalVars.decimateY[trackNum];
		default:
			return 0; // don't need no stinkin errors here
		}
	}

	@Override
	public void setParamLevel(int paramNum, float level) {
		super.setParamLevel(paramNum, level);
		switch (paramNum) {
		case 0: GlobalVars.decimateX[trackNum] = level;
			break;
		case 1: GlobalVars.decimateY[trackNum] = level;
			break;
		default:
			return; // don't need no stinkin errors here
		}
		setDecimateParam(trackNum, paramNum, level);
	}	
	
	@Override
	public String getParamSuffix(int paramNum) {
		switch (paramNum) {
		case 0:
			return "Hz"; // Hz for sample rate
		case 1:
			return ""; // naked label for num bits
		default:
			return ""; // don't need no stinkin errors here
		}		
	}
}
