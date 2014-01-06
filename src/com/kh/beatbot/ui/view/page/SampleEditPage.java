package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.Track;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.SampleEditView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.ParamControl;

public class SampleEditPage extends TouchableView {

	public SampleEditView sampleEdit;
	private ImageButton previewButton, editButton;
	private ToggleButton loopButton, reverseButton;
	private ParamControl loopBeginControl, loopEndControl, gainControl;

	@Override
	public synchronized void update() {
		Track currTrack = TrackManager.currTrack;
		loopButton.setChecked(currTrack.isLooping());
		reverseButton.setChecked(currTrack.isReverse());
		loopBeginControl.setParam(currTrack.getLoopBeginParam());
		loopEndControl.setParam(currTrack.getLoopEndParam());
		gainControl.setParam(currTrack.getGainParam());
		sampleEdit.setParams(currTrack.getLoopBeginParam(), currTrack.getLoopEndParam());
		loopBeginControl.setLabelText("Begin");
		loopEndControl.setLabelText("End");
		gainControl.setLabelText("Gain");

		if (sampleEdit != null)
			sampleEdit.update();
	}

	@Override
	protected synchronized void initIcons() {
		previewButton.setIcon(new Icon(IconResources.PREVIEW));
		loopButton.setIcon(new Icon(IconResources.LOOP));
		reverseButton.setIcon(new Icon(IconResources.REVERSE));

		editButton
				.setBgIcon(new RoundedRectIcon(null, Colors.iconFillColorSet));
		editButton.setIcon(new Icon(IconResources.EDIT));
	}

	@Override
	protected synchronized void createChildren() {
		sampleEdit = new SampleEditView(shapeGroup);
		previewButton = new ImageButton(shapeGroup);
		loopButton = new ToggleButton(shapeGroup);
		reverseButton = new ToggleButton(shapeGroup);
		editButton = new ImageButton(shapeGroup);

		loopBeginControl = new ParamControl(shapeGroup);
		loopEndControl = new ParamControl(shapeGroup);
		gainControl = new ParamControl(shapeGroup);

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

		editButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				BeatBotActivity.mainActivity
						.showDialog(BeatBotActivity.SAMPLE_NAME_EDIT_DIALOG_ID);
			}
		});

		addChildren(previewButton, loopButton, reverseButton, sampleEdit,
				editButton, loopBeginControl, loopEndControl, gainControl);
	}

	@Override
	public void draw() {
		shapeGroup.draw(this);
	}

	@Override
	public synchronized void layoutChildren() {
		float topBarH = height * .29f;
		float fillH = height - topBarH;
		float margin = width * .02f;
		previewButton.layout(this, 0, topBarH, fillH, fillH);
		loopButton.layout(this, width - fillH / 2 - margin, topBarH, fillH / 2,
				fillH / 2);
		reverseButton.layout(this, width - fillH / 2 - margin, height - fillH
				/ 2, fillH / 2, fillH / 2);

		gainControl.layout(this, 0, 0, topBarH * 6, topBarH);
		loopBeginControl.layout(this, topBarH * 6, 0, topBarH * 6, topBarH);
		loopEndControl.layout(this, topBarH * 12, 0, topBarH * 6, topBarH);
		editButton.layout(this, width - topBarH, 0, topBarH, topBarH);

		sampleEdit.layout(this, fillH, topBarH, width - fillH / 2 - fillH
				- margin * 2, fillH);
	}

	@Override
	public synchronized void init() {
		update();
	}
}
