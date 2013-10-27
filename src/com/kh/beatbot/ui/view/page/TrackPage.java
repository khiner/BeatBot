package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.event.TrackDestroyEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;

public class TrackPage extends LevelsFXPage {

	private ImageButton deleteButton;

	@Override
	public void createChildren() {
		super.createChildren();
		deleteButton = new ImageButton();

		deleteButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackDestroyEvent(TrackManager.currTrack).execute();
			}
		});

		children.add(deleteButton);
	}

	@Override
	public void loadIcons() {
		super.loadIcons();

		deleteButton.setBgIcon(new RoundedRectIcon(labelGroup,
				Colors.deleteFillColorSet, Colors.deleteBorderColorSet));
		deleteButton.setIcon(new Icon(IconResources.DELETE_TRACK));
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
		levelBar.layout(this, levelX, topRowY, width - levelX - thirdHeight,
				thirdHeight);

		deleteButton.layout(this, width - thirdHeight, topRowY, thirdHeight,
				thirdHeight);

		effectLabel.layout(this, 0, 13 * height / 24, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, height / 2, 4 * width / 5,
				5 * height / 12);
	}
}
