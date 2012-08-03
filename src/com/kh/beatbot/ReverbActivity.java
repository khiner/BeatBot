package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM] == null) {
			List<EffectParam> params = new ArrayList<EffectParam>();
			params.add(new EffectParam(false, 'x', ""));
			params.add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM] = params;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EFFECT_NUM = 5;
		NUM_PARAMS = 2;
		setContentView(R.layout.reverb_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setReverbOn(trackNum, on);
	}
	
	@Override
	public void setParamNative(int paramNum, float level) {
		setReverbParam(trackNum, paramNum, level);
	}
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbParam(int trackNum, int paramNum, float feedback);
}
