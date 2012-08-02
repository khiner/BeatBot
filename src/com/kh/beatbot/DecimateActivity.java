package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		GlobalVars.params[trackNum].add(new EffectParam(true, 'x', "Hz"));
		GlobalVars.params[trackNum].add(new EffectParam(true, 'y', "Bits"));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 2;
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
	public void setParamNative(int paramNum, float level) {
		setDecimateParam(trackNum, paramNum, level);
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
