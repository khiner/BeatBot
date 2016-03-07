package com.kh.beatbot.ui.view.page.track;

import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.SampleEditView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.ParamControl;

public class SampleEditPage extends TrackPage {
	public SampleEditView sampleEdit;
	private Button previewButton;
	private ToggleButton loopButton, reverseButton;
	private ParamControl loopBeginControl, loopEndControl, gainControl;

	public SampleEditPage(View view) {
		super(view);
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
		sampleEdit = new SampleEditView(this, null);
		sampleEdit.setClip(true);
		previewButton = new Button(this).withIcon(IconResourceSets.PREVIEW);
		loopButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.LOOP);
		reverseButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.REVERSE);

		loopBeginControl = new ParamControl(this);
		loopEndControl = new ParamControl(this);
		gainControl = new ParamControl(this);

		previewButton.setOnPressListener(new OnPressListener() {
			@Override
			public void onPress(Button button) {
				((Track) context.getTrackManager().getCurrTrack()).preview();
			}
		});

		previewButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((Track) context.getTrackManager().getCurrTrack()).stopPreviewing();
			}
		});

		loopButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				((Track) context.getTrackManager().getCurrTrack()).toggleLooping();
			}
		});

		reverseButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				((Track) context.getTrackManager().getCurrTrack()).setReverse(reverseButton
						.isChecked());
			}
		});

		loopBeginControl.setLabelText("Begin");
		loopEndControl.setLabelText("End");
		gainControl.setLabelText("Gain");
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

		gainControl.layout(this, fillH, 0, topBarH * 5, topBarH);
		loopEndControl.layout(this, width - fillH / 2 - margin * 2 - topBarH * 5, 0, topBarH * 5, topBarH);
		loopBeginControl.layout(this, loopEndControl.x - topBarH * 5 - margin, 0, topBarH * 5, topBarH);

		sampleEdit.layout(this, fillH, topBarH, width - fillH / 2 - fillH - margin * 2, fillH);
	}
}
