package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ImageButton;

public class TrackPage extends LevelsFXPage {

	private ImageButton deleteButton;

	@Override
	public void createChildren() {
		super.createChildren();
		deleteButton = new ImageButton();

		deleteButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				// TODO if (MainActivity.prompt)
				TrackManager.deleteCurrTrack();
			}
		});
		
		children.add(deleteButton);
	}

	@Override
	public void loadIcons() {
		super.loadIcons();

		deleteButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				Colors.deleteFillColorSet, Colors.deleteBorderColorSet));
		deleteButton.setIconSource(new ImageIconSource(
				R.drawable.delete_track_icon,
				R.drawable.delete_track_icon_selected));
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float topRowY = height / 12;

		volumeToggle.layout(this, 0, topRowY, 2 * thirdHeight, thirdHeight);
		panToggle.layout(this, 2 * thirdHeight, topRowY, 2 * thirdHeight,
				thirdHeight);
		pitchToggle.layout(this, 4 * thirdHeight, topRowY, 2 * thirdHeight,
				thirdHeight);

		float levelX = 6 * thirdHeight;
		levelBar.layout(this, levelX, topRowY,
				width - levelX - thirdHeight, thirdHeight);
		
		deleteButton.layout(this, width - thirdHeight, topRowY, thirdHeight,
				thirdHeight);

		effectLabel.layout(this, 0, 13 * height / 24, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, height / 2, 4 * width / 5,
				5 * height / 12);
	}
}
