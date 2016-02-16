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
				boolean newRightChannelSynced = delay.getRightTimeParam().isBeatSync();
				float newRightChannelLevel = delay.getRightTimeParam().viewLevel;

				delay.setParamsLinked(linkToggle.isChecked());

				if (delay.paramsLinked()) {
					delay.rightChannelBeatSyncMemory = newRightChannelSynced;
					delay.rightChannelLevelMemory = newRightChannelLevel;
					newRightChannelSynced = delay.getLeftTimeParam().isBeatSync();
					newRightChannelLevel = delay.getLeftTimeParam().viewLevel;
				} else if (delay.rightChannelLevelMemory >= 0) {
					newRightChannelSynced = delay.rightChannelBeatSyncMemory;
					newRightChannelLevel = delay.rightChannelLevelMemory;
				}
				delay.getParam(1).setBeatSync(newRightChannelSynced);
				delay.getParam(1).setLevel(newRightChannelLevel);
				context.getMainPage().effectPage.setLevel2dParams(delay.getXParam(),
						delay.getYParam());
			}
		});
		linkToggle.setChecked(delay.paramsLinked());
		return this;
	}

	public synchronized void layoutChildren() {
		float halfWidth = width / 2;
		float offset = height / 20;
		float paramY = offset;
		float paramH = (height - paramY) / 2 - offset;
		float paramW = 2 * paramH / 3;

		paramControls[0].layout(this, halfWidth - paramW - offset, paramY, paramW, paramH);
		paramControls[1].layout(this, halfWidth + offset, paramY, paramW, paramH);
		paramControls[2].layout(this, halfWidth - paramW - offset, paramY + paramH + offset,
				paramW, paramH);
		paramControls[3].layout(this, halfWidth + offset, paramY + paramH + offset, paramW, paramH);
		float linkH = paramH / 6;
		float linkW = linkH * 2;
		linkToggle.layout(this, halfWidth - linkW / 2 + 2 * offset / 3, paramY + paramH / 2 - linkH
				/ 2, linkW, linkH);
	}

	@Override
	public void onParamChange(Param param) {
		final Delay delay = (Delay) effect;
		final Param leftTimeParam = delay.getLeftTimeParam();
		final Param rightTimeParam = delay.getRightTimeParam();

		if (delay.paramsLinked()) {
			if (param.equals(leftTimeParam)) {
				rightTimeParam.setLevel(param.viewLevel);
			} else if (param.equals(rightTimeParam)) {
				leftTimeParam.ignoreListener(this);
				leftTimeParam.setLevel(param.viewLevel);
				leftTimeParam.unignoreListener(this);
			}
		}
	}

	@Override
	public void onParamToggle(Param param) {
		final Delay delay = (Delay) effect;
		final Param leftTimeParam = delay.getLeftTimeParam();
		final Param rightTimeParam = delay.getRightTimeParam();
		if (delay.paramsLinked()) {
			if (param.equals(leftTimeParam)) {
				rightTimeParam.setBeatSync(param.isBeatSync());
			} else if (param.equals(rightTimeParam)) {
				leftTimeParam.ignoreListener(this);
				leftTimeParam.setBeatSync(param.isBeatSync());
				leftTimeParam.unignoreListener(this);
			}
		}
	}
}
