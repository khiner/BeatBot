package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.AudioMeter;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.view.View;

public class ThresholdBarView extends ControlView1dBase {
	private AudioMeter audioMeter;
	private RoundedRect thresholdSelectTab;

	private float levelBarHeight;

	public ThresholdBarView(View view) {
		super(view);
	}

	@Override
	protected synchronized void createChildren() {
		audioMeter = new AudioMeter(renderGroup, Color.VIEW_BG, null);
		thresholdSelectTab = new RoundedRect(renderGroup, Color.TRON_BLUE_TRANS, null);

		addShapes(audioMeter, thresholdSelectTab);
	}

	@Override
	public synchronized void layoutChildren() {
		levelBarHeight = height / 3;

		thresholdSelectTab.layout(absoluteX, absoluteY + (height - levelBarHeight * 2.5f) / 2,
				levelBarHeight, levelBarHeight * 2.5f);
		audioMeter.layout(absoluteX + thresholdSelectTab.width / 2, absoluteY
				+ (height - levelBarHeight) / 2, width - thresholdSelectTab.width, levelBarHeight);

		if (null != param) {
			onParamChanged(param);
		}
	}

	public void onParamChanged(Param param) {
		float levelPos = levelToX(param.viewLevel);
		thresholdSelectTab.setPosition(absoluteX + levelPos - thresholdSelectTab.width / 2,
				thresholdSelectTab.y);
		audioMeter.setLevel(param.viewLevel);
	}

	protected float posToLevel(Pointer pos) {
		if (pos.x > width - levelBarHeight)
			return 1;
		float level = (pos.x - levelBarHeight / 2) / (width - levelBarHeight * 4);
		return level < 0 ? 0 : (level > 1 ? 1 : level);
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
		param.setLevel(RecordManager.getMaxFrameInRecordSourceBuffer());
		audioMeter.tick();
	}
}