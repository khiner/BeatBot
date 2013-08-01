package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.KnobParamControl;

public class DelayParamsPage extends EffectParamsPage {

	private ToggleButton linkToggle;
	
	public DelayParamsPage(Delay delay) {
		super(delay);
	}
	
	@Override
	public void init() {
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamIndex = 0;
		yParamIndex = 2;
	}

	@Override
	protected void loadIcons() {
		linkToggle.setIcon(new Icon(IconResources.LINK));
	}

	@Override
	public void createChildren() {
		super.createChildren();
		linkToggle = new ToggleButton();
		linkToggle.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				KnobParamControl leftControl = paramControls[0];
				KnobParamControl rightControl = paramControls[1];

				float newRightChannelLevel = rightControl.getLevel();
				boolean newRightChannelSynced = rightControl.isBeatSync();

				effect.setParamsLinked(linkToggle.isChecked());

				if (effect.paramsLinked()) {
					// y = feedback when linked
					yParamIndex = 2;
					((Delay) effect).rightChannelLevelMemory = newRightChannelLevel;
					((Delay) effect).rightChannelBeatSyncMemory = rightControl
							.isBeatSync();
					newRightChannelLevel = leftControl.getLevel();
					newRightChannelSynced = leftControl.isBeatSync();
				} else {
					// y = right delay time when not linked
					yParamIndex = 1;
					newRightChannelSynced = ((Delay) effect).rightChannelBeatSyncMemory;
					if (((Delay) effect).rightChannelLevelMemory > 0)
						newRightChannelLevel = ((Delay) effect).rightChannelLevelMemory;
				}
				effect.getParam(1).beatSync = newRightChannelSynced;
				rightControl.setBeatSync(newRightChannelSynced);
				rightControl.setLevel(newRightChannelLevel);
			}
		});

		addChild(linkToggle);
	}

	@Override
	public void setEffect(Effect effect) {
		super.setEffect(effect);
		linkToggle.setChecked(((Delay) effect).paramsLinked());
	}

	public void layoutChildren() {
		float paramY = 10;
		float paramH = (height - paramY) / 2 - 10;
		float paramW = 2 * paramH / 3;

		paramControls[0].layout(this, width / 2 - paramW - 30, paramY, paramW,
				paramH);
		paramControls[1].layout(this, width / 2 + 30, paramY, paramW, paramH);
		paramControls[2].layout(this, width / 2 - paramW - 30, paramY + paramH,
				paramW, paramH);
		paramControls[3].layout(this, width / 2 + 30, paramY + paramH, paramW,
				paramH);
		float linkH = paramH / 6;
		float linkW = linkH * 2;
		linkToggle.layout(this, width / 2 - linkW / 2, paramY + paramH / 2
				- linkH / 2, linkW, linkH);
	}
}
