package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DecimateActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM] == null) {
			List<EffectParam> params = new ArrayList<EffectParam>();
			params.add(new EffectParam(true, 'x', "Hz"));
			params.add(new EffectParam(true, 'y', "Bits"));
			GlobalVars.params[trackNum][EFFECT_NUM] = params;
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
	
	public void setEffectOn(boolean on) {
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;		
		setDecimateOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setDecimateParam(trackNum, paramNum, level);
	}
	
	public native void setDecimateOn(int trackNum, boolean on);
	public native void setDecimateParam(int trackNum, int paramNum, float param);
}
