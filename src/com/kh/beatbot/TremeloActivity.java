package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class TremeloActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM] == null) {
			List<EffectParam> params = new ArrayList<EffectParam>();
			params.add(new EffectParam(true, 'x', "Hz"));
			params.add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM] = params;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EFFECT_NUM = 6;
		NUM_PARAMS = 2;
		setContentView(R.layout.tremelo_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setTremeloOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setTremeloParam(trackNum, paramNum, level);
	}
	
	public native void setTremeloOn(int trackNum, boolean on);
	public native void setTremeloParam(int trackNum, int paramNum, float param);
}
