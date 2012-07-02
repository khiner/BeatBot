package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class ReverbActivity extends EffectActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.reverbOn[trackNum]);
		((TronSeekbar2d)findViewById(R.id.xyParamBar)).addLevelListener(this);
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

	@Override
	public int getNumParams() {
		return 2;
	}
	
	@Override
	public String getLabelX() {
		return getResources().getString(R.string.hfDamp);
	}

	@Override
	public String getLabelY() {
		return getResources().getString(R.string.feedback);
	}

	@Override
	public String getLabelParam3() {
		return "";
	}
}
