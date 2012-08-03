package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class ChorusActivity extends EffectActivity {

	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, 'x', "Hz"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, 'y', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, ' ', "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ' ', ""));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EFFECT_NUM = 0;
		NUM_PARAMS = 5;
		setContentView(R.layout.chorus_layout);
		initParams();
		((ToggleButton) findViewById(R.id.effectToggleOn))
				.setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setChorusOn(trackNum, on);
	}

	public void setParamNative(int paramNum, float level) {
		setChorusParam(trackNum, paramNum, scaleLevel(level));
	}

	public native void setChorusOn(int trackNum, boolean on);

	public native void setChorusParam(int trackNum, int paramNum, float param);
}
