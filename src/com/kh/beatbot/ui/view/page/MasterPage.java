package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.view.BpmView;
import com.kh.beatbot.ui.view.TextView;

public class MasterPage extends LevelsFXPage {

	private TextView bpmLabel;
	private BpmView bpmView;

	public void setBPM(float bpm) {
		bpmView.setBPM(bpm);
	}

	@Override
	public synchronized void init() {
		setBPM(MidiManager.getBPM());
		update();
	}

	@Override
	public synchronized void createChildren() {
		super.createChildren();
		bpmLabel = new TextView(shapeGroup);
		bpmView = new BpmView(shapeGroup);
		bpmLabel.setText("BPM");
		addChildren(bpmView, bpmLabel);
	}

	@Override
	public synchronized void layoutChildren() {
		float thirdHeight = height / 3;
		float levelHeight = height / 12;

		volumeToggle.layout(this, 0, levelHeight, 2 * thirdHeight, thirdHeight);
		panToggle.layout(this, 2 * thirdHeight, levelHeight, 2 * thirdHeight,
				thirdHeight);
		pitchToggle.layout(this, 4 * thirdHeight, levelHeight, 2 * thirdHeight,
				thirdHeight);
		levelBar.layout(this, 6 * thirdHeight, levelHeight, width - 10
				* thirdHeight, thirdHeight);

		bpmLabel.layout(this, width - 4 * thirdHeight, levelHeight,
				thirdHeight * 2, thirdHeight);
		bpmView.layout(this, width - 2 * thirdHeight, levelHeight,
				2 * thirdHeight, thirdHeight);
		effectLabel.layout(this, 0, 13 * height / 24, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, height / 2, 4 * width / 5,
				5 * height / 12);
	}
}
