package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.view.TronKnob;

public class DelayActivity extends EffectActivity {
	boolean linkChannels = true;
	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	float rightChannelMemory = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamKnob = paramControls.get(0).getKnob();
		yParamKnob = paramControls.get(2).getKnob();
	}
	
	@Override
	public void initParams() {
		EFFECT_NUM = 2;
		NUM_PARAMS = 4;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, ""));
		}
	}

	public void setEffectOnNative(boolean on) {
		setDelayOn(trackNum, on);
	}

	@Override
	public float setParamNative(int paramNum, float level) {
		setDelayParam(trackNum, paramNum, level);
		return level;
	}

	@Override
	public int getEffectLayoutId() {
		return R.layout.delay_layout;
	}
	
	@Override
	public void setLevel(LevelListenable listenable, float level) {
		super.setLevel(listenable, level);
		if (linkChannels) {
			if (listenable.getId() == 0) {
				paramControls.get(1).getKnob().setViewLevel(level);
				paramControls.get(1).setValueLabel(paramControls.get(0).getValueLabel());
			} else if (listenable.getId() == 1) {
				paramControls.get(0).getKnob().setLevel(level);
			}
		}
	}
	
	public void link(View view) {
		TronKnob leftChannelKnob = paramControls.get(0).getKnob();
		TronKnob rightChannelKnob = paramControls.get(1).getKnob();
		float newRightChannelLevel = rightChannelKnob.getLevel();
		linkChannels = !((ToggleButton)view).isChecked();			
		setDelayLinkChannels(trackNum, linkChannels);

		if (linkChannels) {
			// y = feedback when linked
			yParamKnob = paramControls.get(2).getKnob();
			rightChannelMemory = rightChannelKnob.getLevel();
			newRightChannelLevel = leftChannelKnob.getLevel();
		}
		if (!linkChannels) {
			// y = right delay time when not linked
			yParamKnob = paramControls.get(1).getKnob();
			if (rightChannelMemory > 0)
				newRightChannelLevel = rightChannelMemory;
		}
		rightChannelKnob.setLevel(newRightChannelLevel);
	}
	
	public native void setDelayOn(int trackNum, boolean on);
	public native void setDelayLinkChannels(int trackNum, boolean link);
	public native void setDelayParam(int trackNum, int paramNum, float param);
}
