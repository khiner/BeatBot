package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.SampleEditView;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.ValueLabel;

public class SampleEditPage extends Page {

	private ShapeGroup labelGroup = new ShapeGroup();

	private SampleEditView sampleEdit;
	private ImageButton previewButton, browseButton, editButton;
	private ToggleButton loopButton, reverseButton;
	private TextView loopBeginLabel, loopEndLabel;
	private ValueLabel loopBeginControl, loopEndControl;
	
	@Override
	public void update() {
		if (sampleEdit != null)
			sampleEdit.update();
		loopButton.setChecked(TrackManager.currTrack.isLooping());
		reverseButton.setChecked(TrackManager.currTrack.isReverse());
	}

	@Override
	protected void loadIcons() {
		previewButton.setIcon(new Icon(IconResources.PREVIEW));
		loopButton.setIcon(new Icon(IconResources.LOOP));
		reverseButton.setIcon(new Icon(IconResources.REVERSE));

		browseButton.setBgIcon(new RoundedRectIcon(labelGroup,
				Colors.iconFillColorSet));
		browseButton.setIcon(new Icon(IconResources.BROWSE));

		editButton.setBgIcon(new RoundedRectIcon(labelGroup,
				Colors.iconFillColorSet));
		editButton.setIcon(new Icon(IconResources.EDIT));
	}

	@Override
	public void draw() {
		labelGroup.draw(this, 1);
	}

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditView();
		previewButton = new ImageButton();
		loopButton = new ToggleButton();
		reverseButton = new ToggleButton();
		browseButton = new ImageButton();
		editButton = new ImageButton();

		loopBeginLabel = new ImageButton();
		loopEndLabel = new ImageButton();
		loopBeginControl = new ValueLabel();
		loopEndControl = new ValueLabel();
				
		previewButton.setOnPressListener(new OnPressListener() {
			@Override
			public void onPress(Button button) {
				TrackManager.currTrack.preview();
			}
		});

		previewButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				TrackManager.currTrack.stopPreviewing();
			}
		});
		loopButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				TrackManager.currTrack.toggleLooping();
			}
		});
		reverseButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				TrackManager.currTrack.setReverse(reverseButton.isChecked());
			}
		});

		browseButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				DirectoryManager.showInstrumentSelectAlert();
			}
		});

		editButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				BeatBotActivity.mainActivity
						.showDialog(BeatBotActivity.SAMPLE_NAME_EDIT_DIALOG_ID);
			}
		});

		loopBeginLabel.setText("Begin");
		loopEndLabel.setText("End");
		loopBeginControl.initBgRect(labelGroup, Colors.LABEL_VERY_LIGHT, Colors.BLACK);
		loopEndControl.initBgRect(labelGroup, Colors.LABEL_VERY_LIGHT, Colors.BLACK);
		addChild(previewButton);
		addChild(loopButton);
		addChild(reverseButton);
		addChild(sampleEdit);
		addChild(browseButton);
		addChild(editButton);
		addChild(loopBeginLabel);
		addChild(loopEndLabel);
		addChild(loopBeginControl);
		addChild(loopEndControl);
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float margin = 7;
		previewButton.layout(this, 0, 0, thirdHeight, thirdHeight);
		loopButton.layout(this, thirdHeight + margin, 0, thirdHeight,
				thirdHeight);
		reverseButton.layout(this, thirdHeight * 2 + margin * 2, 0,
				thirdHeight, thirdHeight);

		loopBeginLabel.layout(this, width - thirdHeight * 15, 0,
				thirdHeight * 3, thirdHeight);
		loopBeginControl.layout(this, width - thirdHeight * 12, 0,
				thirdHeight * 3, thirdHeight);
		loopEndLabel.layout(this, width - thirdHeight * 9, 0, thirdHeight * 3,
				thirdHeight);
		loopEndControl.layout(this, width - thirdHeight * 6, 0,
				thirdHeight * 3, thirdHeight);
		browseButton.layout(this, width - thirdHeight * 2, 0, thirdHeight,
				thirdHeight);
		editButton.layout(this, width - thirdHeight, 0, thirdHeight,
				thirdHeight);

		sampleEdit.layout(this, 0, thirdHeight, width, 2 * thirdHeight);
	}
}
