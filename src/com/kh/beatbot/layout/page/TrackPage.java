package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ImageButton;

public class TrackPage extends LevelsFXPage {

	private ImageButton browseButton;

	@Override
	public void createChildren() {
		super.createChildren();
		browseButton = new ImageButton();
		browseButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				Managers.directoryManager.showInstrumentSelectAlert();
			}
		});
		children.add(browseButton);
	}

	@Override
	public void loadIcons() {
		super.loadIcons();
		browseButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				Colors.instrumentBgColorSet, Colors.instrumentStrokeColorSet));
		browseButton.setIconSource(new ImageIconSource(R.drawable.browse_icon,
				R.drawable.browse_icon_selected));
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float levelHeight = height / 12;
		float effectHeight = height - height / 12 - thirdHeight;
		float gapBetweenLabels = 5;

		browseButton.layout(this, 0, levelHeight, thirdHeight, thirdHeight);

		volumeToggle.layout(this, gapBetweenLabels + thirdHeight, levelHeight,
				2 * thirdHeight, thirdHeight);
		panToggle.layout(this, gapBetweenLabels * 2 + 3 * thirdHeight,
				levelHeight, 2 * thirdHeight, thirdHeight);
		pitchToggle.layout(this, gapBetweenLabels * 3 + 5 * thirdHeight,
				levelHeight, 2 * thirdHeight, thirdHeight);
		float levelX = 7 * thirdHeight + gapBetweenLabels * 4;
		levelBar.layout(this, levelX, levelHeight, width - levelX, thirdHeight);
		effectLabel.layout(this, 0, effectHeight, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, effectHeight, 4 * width / 5,
				thirdHeight);
	}
}
