package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.AudioMeter;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.view.View;

public class ThresholdBar extends ControlView1dBase {
	private AudioMeter audioMeter;
	private RoundedRect thresholdSelectTab;

	private float levelBarHeight;

	public ThresholdBar(View view) {
		super(view);
	}

	@Override
	protected void createChildren() {
		audioMeter = new AudioMeter(renderGroup, Color.VIEW_BG, null);
		thresholdSelectTab = new RoundedRect(renderGroup, Color.TRON_BLUE_TRANS, null);

		addShapes(audioMeter, thresholdSelectTab);
	}

	@Override
	public void layoutChildren() {
		levelBarHeight = height / 3;

		thresholdSelectTab.layout(absoluteX, absoluteY + (height - levelBarHeight * 2.5f) / 2,
				levelBarHeight, levelBarHeight * 2.5f);
		audioMeter.layout(absoluteX + thresholdSelectTab.width / 2, absoluteY
				+ (height - levelBarHeight) / 2, width - thresholdSelectTab.width, levelBarHeight);

		if (null != param) {
			onParamChange(param);
		}
	}

	public void onParamChange(Param param) {
		thresholdSelectTab.setPosition(absoluteX + levelToX(param.viewLevel) - thresholdSelectTab.width / 2,
				thresholdSelectTab.y);
	}

	// normalized level is [0,1]. Param handles DB conversion automagically
	public void setLevelNormalized(float levelNormalized) {
		audioMeter.setLevel(levelNormalized);
	}

	protected float posToLevel(Pointer pos) {
		if (pos.x > width - thresholdSelectTab.width / 2)
			return 1;
		float level = (pos.x - thresholdSelectTab.width / 2) / (width - thresholdSelectTab.width);
		return GeneralUtils.clipToUnit(level);
	}

	private float levelToX(float viewLevel) {
		return thresholdSelectTab.width / 2 + viewLevel * (width - thresholdSelectTab.width);
	}

	@Override
	public void press() {
		super.press();
		thresholdSelectTab.setFillColor(selectColorTrans);
	}

	@Override
	public void release() {
		super.release();
		thresholdSelectTab.setFillColor(levelColorTrans);
	}

	@Override
	public void tick() {
		audioMeter.tick();
	}
}