package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class DelayParamsPage extends EffectParamsPage {
	private ToggleButton linkToggle;

	public DelayParamsPage(View view) {
		super(view);
		linkToggle = new ToggleButton(this).oscillating().withIcon(IconResourceSets.LINK);
	}

	@Override
	public DelayParamsPage withEffect(final Effect effect) {
		super.withEffect(effect);

		linkToggle.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				boolean newRightChannelSynced = effect.getParam(1).isBeatSync();
				float newRightChannelLevel = effect.getParam(1).viewLevel;

				effect.setParamsLinked(linkToggle.isChecked());

				if (effect.paramsLinked()) {
					((Delay) effect).rightChannelBeatSyncMemory = newRightChannelSynced;
					((Delay) effect).rightChannelLevelMemory = newRightChannelLevel;
					newRightChannelSynced = effect.getParam(0).isBeatSync();
					newRightChannelLevel = effect.getParam(0).viewLevel;
				} else if (((Delay) effect).rightChannelLevelMemory >= 0) {
					newRightChannelSynced = ((Delay) effect).rightChannelBeatSyncMemory;
					newRightChannelLevel = ((Delay) effect).rightChannelLevelMemory;
				}
				effect.getParam(1).toggle(newRightChannelSynced);
				effect.getParam(1).setLevel(newRightChannelLevel);
				effectPage.setLevel2dParams(effect.getXParam(), effect.getYParam());
			}
		});

		linkToggle.setChecked(effect.paramsLinked());
		return this;
	}

	public synchronized void layoutChildren() {
		float offset = height / 20;
		float paramY = offset;
		float paramH = (height - paramY) / 2 - offset;
		float paramW = 2 * paramH / 3;

		paramControls[0].layout(this, width / 2 - paramW - offset, paramY, paramW, paramH);
		paramControls[1].layout(this, width / 2 + offset, paramY, paramW, paramH);
		paramControls[2].layout(this, width / 2 - paramW - offset, paramY + paramH + offset, paramW, paramH);
		paramControls[3].layout(this, width / 2 + offset, paramY + paramH + offset, paramW, paramH);
		float linkH = paramH / 6;
		float linkW = linkH * 2;
		linkToggle.layout(this, width / 2 - linkW / 2 + offset / 2, paramY + paramH / 2 - linkH / 2, linkW,
				linkH);
	}
}
