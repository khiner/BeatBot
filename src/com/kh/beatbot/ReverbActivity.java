package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		GlobalVars.params[trackNum].add(new EffectParam(false, 'x', ""));
		GlobalVars.params[trackNum].add(new EffectParam(false, 'y', ""));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 2;
		setContentView(R.layout.reverb_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.reverbOn[trackNum]);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.reverbOn[trackNum] = on;
		setReverbOn(trackNum, on);
	}
	
	@Override
	public void setParamNative(int paramNum, float level) {
		setReverbParam(trackNum, paramNum, level);
	}
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbParam(int trackNum, int paramNum, float feedback);
}
