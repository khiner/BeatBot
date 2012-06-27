package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;

public class ReverbActivity extends EffectActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		initLevelBars();
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.reverbOn[trackNum]);
	}
	
	public float getXValue() {
		return GlobalVars.reverbX[trackNum];
	}

	public float getYValue() {
		return GlobalVars.reverbY[trackNum];
	}

	public void setXValue(float xValue) {
		GlobalVars.reverbX[trackNum] = xValue;
		setReverbHfDamp(trackNum, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.reverbY[trackNum] = yValue;
		setReverbFeedback(trackNum, yValue);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.reverbOn[trackNum] = on;
		setReverbOn(trackNum, on);
	}

	public void setEffectDynamic(boolean dynamic) {
		return;  // reverb can only be dynamic
	}
	
	public native void setReverbOn(int trackNum, boolean on);
	public native void setReverbFeedback(int trackNum, float feedback);
	public native void setReverbHfDamp(int trackNum, float hfDamp);
}
