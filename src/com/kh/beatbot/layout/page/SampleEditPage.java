package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.SampleEditBBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ImageButton;
import com.kh.beatbot.view.control.ToggleButton;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class SampleEditPage extends Page {

	private ShapeGroup labelGroup;

	private SampleEditBBView sampleEdit;
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
		previewButton.setIconSource(new ImageIconSource(
				R.drawable.preview_icon, R.drawable.preview_icon_selected));
		loopButton.setIconSource(new ImageIconSource(R.drawable.loop_icon,
				R.drawable.loop_selected_icon));
		reverseButton.setIconSource(new ImageIconSource(
				R.drawable.reverse_icon, R.drawable.reverse_selected_icon));

		browseButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				Colors.iconFillColorSet, Colors.iconBorderColorSet));
		browseButton.setIconSource(new ImageIconSource(R.drawable.browse_icon,
				R.drawable.browse_icon_selected));

		editButton.setBgIconSource(new RoundedRectIconSource(labelGroup,
				Colors.iconFillColorSet, Colors.iconBorderColorSet));
		editButton.setIconSource(new ImageIconSource(R.drawable.edit_icon,
				R.drawable.edit_icon_selected));
	}

	@Override
	public void draw() {
		// parent view - no drawing to do
	}

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditBBView();
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
