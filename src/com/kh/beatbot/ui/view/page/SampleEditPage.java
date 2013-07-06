package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.GlobalVars;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.SampleEditView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class SampleEditPage extends Page {

	private ShapeGroup labelGroup;

	private SampleEditView sampleEdit;
	private ImageButton previewButton, browseButton, editButton;
	private ToggleButton loopButton, reverseButton;

	public void init() {
	}

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
				Colors.iconFillColorSet, Colors.iconBorderColorSet));
		browseButton.setIcon(new Icon(IconResources.BROWSE));

		editButton.setBgIcon(new RoundedRectIcon(labelGroup,
				Colors.iconFillColorSet, Colors.iconBorderColorSet));
		editButton.setIcon(new Icon(IconResources.EDIT));
	}

	@Override
	public void draw() {
		// parent view - no drawing to do
	}

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditView();
		previewButton = new ImageButton();
		loopButton = new ToggleButton();
		reverseButton = new ToggleButton();
		browseButton = new ImageButton();
		editButton = new ImageButton();
		
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
				Managers.directoryManager.showInstrumentSelectAlert();
			}
		});
		editButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				GlobalVars.mainActivity
						.showDialog(BeatBotActivity.SAMPLE_NAME_EDIT_DIALOG_ID);
			}
		});

		addChild(previewButton);
		addChild(loopButton);
		addChild(reverseButton);
		addChild(sampleEdit);
		addChild(browseButton);
		addChild(editButton);
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		previewButton.layout(this, 0, 0, thirdHeight, thirdHeight);
		loopButton.layout(this, thirdHeight, 0, thirdHeight, thirdHeight);
		reverseButton
				.layout(this, thirdHeight * 2, 0, thirdHeight, thirdHeight);

		browseButton.layout(this, width - thirdHeight * 2, 0, thirdHeight,
				thirdHeight);
		editButton.layout(this, width - thirdHeight, 0, thirdHeight,
				thirdHeight);

		sampleEdit.layout(this, 0, thirdHeight, width, 2 * thirdHeight);
	}
}
