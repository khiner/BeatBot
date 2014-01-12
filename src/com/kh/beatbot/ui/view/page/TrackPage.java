package com.kh.beatbot.ui.view.page;

public class TrackPage extends LevelsFXPage {

	@Override
	public synchronized void layoutChildren() {
		float thirdHeight = height / 3;
		float topRowY = height / 12;

		volumeToggle.layout(this, 0, topRowY, 2 * thirdHeight, thirdHeight);
		panToggle.layout(this, 2 * thirdHeight, topRowY, 2 * thirdHeight,
				thirdHeight);
		pitchToggle.layout(this, 4 * thirdHeight, topRowY, 2 * thirdHeight,
				thirdHeight);

		float levelX = 6 * thirdHeight;
		levelBar.layout(this, levelX, topRowY, width - levelX, thirdHeight);

		effectLabel.layout(this, 0, 13 * height / 24, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, height / 2, 4 * width / 5,
				5 * height / 12);
	}
}
