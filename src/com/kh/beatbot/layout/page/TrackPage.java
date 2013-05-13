package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ImageButton;

public class TrackPage extends LevelsFXPage {

	private ImageButton browseButton, editButton, deleteButton;

	@Override
	public void createChildren() {
		super.createChildren();
		browseButton = new ImageButton();
		editButton = new ImageButton();
		deleteButton = new ImageButton();
		browseButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				Managers.directoryManager.showInstrumentSelectAlert();
			}
		});
		editButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				String name = "TODO";
				TrackManager.currTrack.setCurrSampleName(name);
			}
		});
		deleteButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				TrackManager.deleteCurrTrack();
			}
		});
		children.add(browseButton);
		children.add(deleteButton);
		children.add(editButton);
	}

	@Override
	public void loadIcons() {
		super.loadIcons();
		ColorSet iconFillColorSet = new ColorSet(Colors.TRANSPARANT,
				Colors.LABEL_SELECTED);
		ColorSet iconBorderColorSet = new ColorSet(Colors.WHITE, Colors.BLACK);

		ColorSet deleteFillColorSet = new ColorSet(Colors.TRANSPARANT,
				Colors.RED);
		ColorSet deleteBorderColorSet = new ColorSet(Colors.RED, Colors.BLACK);

		browseButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				iconFillColorSet, iconBorderColorSet));
		browseButton.setIconSource(new ImageIconSource(R.drawable.browse_icon,
				R.drawable.browse_icon_selected));

		editButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				iconFillColorSet, iconBorderColorSet));
		editButton.setIconSource(new ImageIconSource(R.drawable.edit_icon,
				R.drawable.edit_icon_selected));

		deleteButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				deleteFillColorSet, deleteBorderColorSet));
		deleteButton.setIconSource(new ImageIconSource(
				R.drawable.delete_track_icon,
				R.drawable.delete_track_icon_selected));
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float topRowY = height / 12;
		float gapBetweenLabels = 5;

		browseButton.layout(this, 0, topRowY, thirdHeight, thirdHeight);
		editButton.layout(this, thirdHeight, topRowY, thirdHeight, thirdHeight);
		
		volumeToggle.layout(this, gapBetweenLabels + thirdHeight * 2, topRowY,
				2 * thirdHeight, thirdHeight);
		panToggle.layout(this, gapBetweenLabels + 4 * thirdHeight, topRowY,
				2 * thirdHeight, thirdHeight);
		pitchToggle.layout(this, gapBetweenLabels + 6 * thirdHeight,
				topRowY, 2 * thirdHeight, thirdHeight);

		float levelX = 8 * thirdHeight + gapBetweenLabels;
		levelBar.layout(this, levelX, topRowY, width - levelX - thirdHeight,
				thirdHeight);
		deleteButton.layout(this, width - thirdHeight, topRowY, thirdHeight,
				thirdHeight);

		effectLabel.layout(this, 0, 13 * height / 24, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, height / 2, 4 * width / 5,
				5 * height / 12);
	}
}
