package com.kh.beatbot.ui.view.group.effect;

import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.ParamControl;

public class DelayParamsGroup extends EffectParamsGroup implements ParamListener,
		ParamToggleListener {
	private ToggleButton linkToggle;

	public DelayParamsGroup(View view) {
		super(view);
		linkToggle = new ToggleButton(this).oscillating().withIcon(IconResourceSets.LINK);
	}

	@Override
	public DelayParamsGroup withEffect(final Effect effect) {
		super.withEffect(effect);

		final Delay delay = (Delay) effect;

		for (ParamControl paramControl : paramControls) {
			delay.getParam(paramControl.getId()).removeListener(this);
			delay.getParam(paramControl.getId()).removeToggleListener(this);
			delay.getParam(paramControl.getId()).addListener(this);
			delay.getParam(paramControl.getId()).addToggleListener(this);
		}

		linkToggle.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				boolean newRightChannelSynced = delay.getParam(1).isBeatSync();
				float newRightChannelLevel = delay.getParam(1).viewLevel;

				delay.setParamsLinked(linkToggle.isChecked());

				if (delay.paramsLinked()) {
					delay.rightChannelBeatSyncMemory = newRightChannelSynced;
					delay.rightChannelLevelMemory = newRightChannelLevel;
					newRightChannelSynced = delay.getParam(0).isBeatSync();
					newRightChannelLevel = delay.getParam(0).viewLevel;
				} else if (delay.rightChannelLevelMemory >= 0) {
					newRightChannelSynced = delay.rightChannelBeatSyncMemory;
					newRightChannelLevel = delay.rightChannelLevelMemory;
				}
				delay.getParam(1).setBeatSync(newRightChannelSynced);
				delay.getParam(1).setLevel(newRightChannelLevel);
				mainPage.effectPage.setLevel2dParams(delay.getXParam(), delay.getYParam());
			}
		});
		linkToggle.setChecked(delay.paramsLinked());
		return this;
	}

	public synchronized void layoutChildren() {
		float offset = height / 20;
		float paramY = offset;
		float paramH = (height - paramY) / 2 - offset;
		float paramW = 2 * paramH / 3;

		paramControls[0].layout(this, width / 2 - paramW - offset, paramY, paramW, paramH);
		paramControls[1].layout(this, width / 2 + offset, paramY, paramW, paramH);
		paramControls[2].layout(this, width / 2 - paramW - offset, paramY + paramH + offset,
				paramW, paramH);
		paramControls[3].layout(this, width / 2 + offset, paramY + paramH + offset, paramW, paramH);
		float linkH = paramH / 6;
		float linkW = linkH * 2;
		linkToggle.layout(this, width / 2 - linkW / 2 + offset / 2,
				paramY + paramH / 2 - linkH / 2, linkW, linkH);
	}

	@Override
	public void onParamChange(Param param) {
		Delay delay = (Delay) effect;
		if (delay.paramsLinked()) {
			if (param.id == 0) {
				delay.getParam(1).setLevel(param.viewLevel);
			} else if (param.id == 1) {
				delay.getParam(0).ignoreListener(this);
				delay.getParam(0).setLevel(param.viewLevel);
				delay.getParam(0).unignoreListener(this);
			}
		}
	}

	@Override
	public void onParamToggle(Param param) {
		Delay delay = (Delay) effect;
		if (delay.paramsLinked()) {
			if (param.id == 0) {
				delay.getParam(1).setBeatSync(param.isBeatSync());
			} else if (param.id == 1) {
				delay.getParam(0).ignoreListener(this);
				delay.getParam(0).setBeatSync(param.isBeatSync());
				delay.getParam(0).unignoreListener(this);
			}
		}
	}
}
