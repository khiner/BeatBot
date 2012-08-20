package com.kh.beatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.view.TronKnob;
import com.kh.beatbot.view.TronSeekbar2d;

public class DelayActivity extends EffectActivity {
	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	private float rightChannelLevelMemory = -1;
	private boolean rightChannelBeatSyncMemory = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamKnob = paramControls.get(0).getKnob();
		yParamKnob = GlobalVars.delayParamsLinked[trackNum] ? paramControls.get(2).getKnob() : paramControls.get(1).getKnob();
		((ToggleButton)findViewById(R.id.linkButton)).setChecked(GlobalVars.delayParamsLinked[trackNum]);
	}
	
	@Override
	public void initParams() {
		EFFECT_NUM = GlobalVars.DELAY_EFFECT_NUM;
		NUM_PARAMS = 4;
		if (GlobalVars.params[trackNum][EFFECT_NUM].isEmpty()) {
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(true, true, "ms"));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
			GlobalVars.params[trackNum][EFFECT_NUM].add(new EffectParam(false, false, ""));
		}
	}

	@Override
	public int getParamLayoutId() {
		return R.layout.delay_param_layout;
	}
	
	@Override
	public void setLevel(LevelListenable listenable, float level) {
		super.setLevel(listenable, level);
		if (GlobalVars.delayParamsLinked[trackNum]) {
			if (listenable.getId() == 0) {
				setParamLevel(1, level);
				paramControls.get(1).getKnob().setViewLevel(level);
				paramControls.get(1).setValueLabel(paramControls.get(0).getValueLabel());
			} else if (listenable.getId() == 1) {
				paramControls.get(0).getKnob().setLevel(level);
			}
		}
	}
	
	@Override
	public final void notifyInit(LevelListenable listenable) {
		super.notifyInit(listenable);
		if (!(listenable instanceof TronSeekbar2d)) {
			EffectParam param = getParam(listenable.getId());
			((TronKnob)listenable).setBeatSync(param.beatSync);
		}
	}
	
	@Override
	public void notifyClicked(LevelListenable listenable) {
		super.notifyClicked(listenable);
		if (!(listenable instanceof TronSeekbar2d)) {
			int paramNum = listenable.getId();
			if (GlobalVars.delayParamsLinked[trackNum]) {
				EffectParam param = getParam(paramNum);
				if (paramNum == 0) {
					getParam(1).beatSync = param.beatSync;
					paramControls.get(1).getKnob().setBeatSync(param.beatSync);
					paramControls.get(1).getKnob().setLevel(param.viewLevel);
				} else if (paramNum == 1) {
					getParam(0).beatSync = param.beatSync;
					paramControls.get(0).getKnob().setBeatSync(param.beatSync);
					paramControls.get(0).getKnob().setLevel(param.viewLevel);
				}
			}
		}
	}
	
	public void link(View view) {
		TronKnob leftChannelKnob = paramControls.get(0).getKnob();
		TronKnob rightChannelKnob = paramControls.get(1).getKnob();
		float newRightChannelLevel = rightChannelKnob.getLevel();
		boolean newRightChannelSynced = rightChannelKnob.isBeatSync();
		boolean linkChannels = ((ToggleButton)view).isChecked();
		
		GlobalVars.delayParamsLinked[trackNum] = linkChannels;
		setDelayLinkChannels(trackNum, linkChannels);

		if (linkChannels) {
			// y = feedback when linked
			yParamKnob = paramControls.get(2).getKnob();
			rightChannelLevelMemory = rightChannelKnob.getLevel();
			rightChannelBeatSyncMemory = rightChannelKnob.isBeatSync();
			newRightChannelLevel = leftChannelKnob.getLevel();
			newRightChannelSynced = leftChannelKnob.isBeatSync();
		}
		if (!linkChannels) {
			// y = right delay time when not linked
			yParamKnob = paramControls.get(1).getKnob();
			newRightChannelSynced = rightChannelBeatSyncMemory;
			if (rightChannelLevelMemory > 0)
				newRightChannelLevel = rightChannelLevelMemory;
		}
		rightChannelKnob.setLevel(newRightChannelLevel);
		rightChannelKnob.setBeatSync(newRightChannelSynced);
		getParam(1).beatSync = newRightChannelSynced;
	}
	
	public static native void setDelayLinkChannels(int trackNum, boolean link);
}
