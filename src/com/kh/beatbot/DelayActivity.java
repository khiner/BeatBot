package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class DelayActivity extends EffectActivity {
	
	@Override
	public void initParams() {
		super.initParams();
		GlobalVars.params[trackNum].add(new EffectParam(true, 'x', "ms"));
		GlobalVars.params[trackNum].add(new EffectParam(false, 'y', ""));
		GlobalVars.params[trackNum].add(new EffectParam(false, ' ', ""));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 3;
		setContentView(R.layout.delay_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.delayOn[trackNum]);
	}
		
	public void setEffectOn(boolean on) {
		GlobalVars.delayOn[trackNum] = on;
		setDelayOn(trackNum, on);
	}
	
	public void beatMatch(View view) {
		boolean beatmatch = ((ToggleButton)view).isChecked();
		GlobalVars.delayBeatmatch[trackNum] = beatmatch;
	}
	
	@Override
	public void setParamNative(int paramNum, float level) {
		setDelayParam(trackNum, paramNum, level);
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayParam(int trackNum, int paramNum, float param);
}
