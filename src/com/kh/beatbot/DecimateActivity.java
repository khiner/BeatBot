package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'y', "Bits"));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EFFECT_NUM = 1;
		NUM_PARAMS = 2;
		setContentView(R.layout.decimate_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
	}
		
	public boolean isEffectOn() {
		return GlobalVars.effectOn[trackNum][EFFECT_NUM];
	}
	
	public void setEffectOnNative(boolean on) {
		setDecimateOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setDecimateParam(trackNum, paramNum, level);
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
