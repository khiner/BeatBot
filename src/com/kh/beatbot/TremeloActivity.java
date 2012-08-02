package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class TremeloActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		GlobalVars.params[trackNum].add(new EffectParam(true, 'x', "Hz"));
		GlobalVars.params[trackNum].add(new EffectParam(false, 'y', ""));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 2;
		setContentView(R.layout.tremelo_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.tremeloOn[trackNum]);
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.tremeloOn[trackNum] = on;
		setTremeloOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setTremeloParam(trackNum, paramNum, level);
	}
	
	public native void setTremeloOn(int trackNum, boolean on);
	public native void setTremeloParam(int trackNum, int paramNum, float param);
}
