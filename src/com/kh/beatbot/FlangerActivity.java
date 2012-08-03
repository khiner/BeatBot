package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FlangerActivity extends EffectActivity {
	@Override
	public void initParams() {
		super.initParams();
		if (GlobalVars.params[trackNum][EFFECT_NUM] == null) {
			List<EffectParam> params = new ArrayList<EffectParam>();
			params.add(new EffectParam(true, 'x', "ms"));
			params.add(new EffectParam(false, 'y', ""));
			params.add(new EffectParam(false, ' ', ""));
			params.add(new EffectParam(true, ' ', "Hz"));
			params.add(new EffectParam(false, ' ', ""));
			GlobalVars.params[trackNum][EFFECT_NUM] = params;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EFFECT_NUM = 4;
		NUM_PARAMS = 6;
		setContentView(R.layout.flanger_layout);
		initParams();
		((ToggleButton) findViewById(R.id.effectToggleOn))
				.setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
	}

	public void setEffectOn(boolean on) {
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setFlangerOn(trackNum, on);
	}

	@Override
	public void setParamNative(int paramNum, float level) {
		setFlangerParam(trackNum, paramNum, level);
	}

	public native void setFlangerOn(int trackNum, boolean on);

	public native void setFlangerParam(int trackNum, int paramNum, float param);
}
