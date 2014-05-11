package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.SampleEditView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.ParamControl;

public class SampleEditPage extends TrackPage {

	public SampleEditView sampleEdit;
	private Button previewButton;
	private ToggleButton loopButton, reverseButton;
	private ParamControl loopBeginControl, loopEndControl, gainControl;

	public SampleEditPage(RenderGroup renderGroup) {
		super(renderGroup);
	}

	@Override
	public void onSelect(BaseTrack track) {
		Track currTrack = (Track) track;
		loopButton.setChecked(currTrack.isLooping());
		reverseButton.setChecked(currTrack.isReverse());
		loopBeginControl.setParam(currTrack.getLoopBeginParam());
		loopEndControl.setParam(currTrack.getLoopEndParam());
		gainControl.setParam(currTrack.getGainParam());

		if (null != sampleEdit) {
			sampleEdit.setParams(currTrack.getLoopBeginParam(), currTrack.getLoopEndParam());
			sampleEdit.update();
		}
	}

	@Override
	protected synchronized void createChildren() {
		sampleEdit = new SampleEditView(null);
		sampleEdit.setClip(true);
		previewButton = new Button(renderGroup);
		loopButton = new ToggleButton(renderGroup).oscillating();
		reverseButton = new ToggleButton(renderGroup).oscillating();

		loopBeginControl = new ParamControl(renderGroup);
		loopEndControl = new ParamControl(renderGroup);
		gainControl = new ParamControl(renderGroup);

		previewButton.setIcon(IconResourceSets.PREVIEW);
		loopButton.setIcon(IconResourceSets.LOOP);
		reverseButton.setIcon(IconResourceSets.REVERSE);

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

		loopBeginControl.setLabelText("Begin");
		loopEndControl.setLabelText("End");
		gainControl.setLabelText("Gain");

		addChildren(previewButton, loopButton, reverseButton, sampleEdit, loopBeginControl,
				loopEndControl, gainControl);
	}

	@Override
	public synchronized void layoutChildren() {
		float topBarH = height * .29f;
		float fillH = height - topBarH;
		float margin = width * .02f;
		previewButton.layout(this, 0, topBarH, fillH, fillH);
		loopButton.layout(this, width - fillH / 2 - margin, topBarH, fillH / 2, fillH / 2);
		reverseButton.layout(this, width - fillH / 2 - margin, height - fillH / 2, fillH / 2,
				fillH / 2);

		gainControl.layout(this, 0, 0, topBarH * 6, topBarH);
		loopBeginControl.layout(this, topBarH * 6, 0, topBarH * 6, topBarH);
		loopEndControl.layout(this, topBarH * 12, 0, topBarH * 6, topBarH);

		sampleEdit.layout(this, fillH, topBarH, width - fillH / 2 - fillH - margin * 2, fillH);
	}
}
