package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FlangerActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		GlobalVars.params[trackNum].add(new EffectParam(true, 'x', "ms"));
		GlobalVars.params[trackNum].add(new EffectParam(false, 'y', ""));
		GlobalVars.params[trackNum].add(new EffectParam(false, ' ', ""));
		GlobalVars.params[trackNum].add(new EffectParam(true, ' ', "Hz"));
		GlobalVars.params[trackNum].add(new EffectParam(false, ' ', ""));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 6;
		setContentView(R.layout.flanger_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.flangerOn[trackNum]);
	}
			
	public void setEffectOn(boolean on) {
		GlobalVars.flangerOn[trackNum] = on;
		setFlangerOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setFlangerParam(trackNum, paramNum, level);
	}
	
	public native void setFlangerOn(int trackNum, boolean on);
	public native void setFlangerParam(int trackNum, int paramNum, float param);
}
