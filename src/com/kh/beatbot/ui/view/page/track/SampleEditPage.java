package com.kh.beatbot.ui.view.page.track;

import com.kh.beatbot.event.track.TrackGainSetEvent;
import com.kh.beatbot.event.track.TrackLoopWindowSetEvent;
import com.kh.beatbot.event.track.TrackToggleLoopEvent;
import com.kh.beatbot.event.track.TrackToggleReverseEvent;
import com.kh.beatbot.listener.MultiViewTouchTracker;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.TouchableViewListener;
import com.kh.beatbot.listener.TouchableViewsListener;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.SampleEditView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.ParamControl;

public class SampleEditPage extends TrackPage implements TouchableViewsListener {
	public SampleEditView sampleEdit;
	private Button previewButton;
	private ToggleButton loopButton, reverseButton;
	private ParamControl loopBeginControl, loopEndControl, gainControl;
	private TrackLoopWindowSetEvent loopWindowEvent;
	private TrackGainSetEvent gainEvent;

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
	public void onReverseChange(Track track, boolean reverse) {
		reverseButton.setChecked(reverse);
	}

	@Override
	public void onLoopChange(Track track, boolean loop) {
		loopButton.setChecked(loop);
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

		gainControl.setListener(new TouchableViewListener() {
			@Override
			public void onPress(TouchableView view) {
				gainEvent = new TrackGainSetEvent(context.getTrackManager().getCurrTrack()
						.getId());
				gainEvent.begin();
			}

			@Override
			public void onRelease(TouchableView view) {
				gainEvent.end();
			}
		});

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
				new TrackToggleLoopEvent((Track) context.getTrackManager().getCurrTrack())
						.execute();
			}
		});

		reverseButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				new TrackToggleReverseEvent((Track) context.getTrackManager().getCurrTrack())
						.execute();
			}
		});

		loopBeginControl.setLabelText("Begin");
		loopEndControl.setLabelText("End");
		gainControl.setLabelText("Gain");

		new MultiViewTouchTracker(this).monitorViews(loopBeginControl, loopEndControl, sampleEdit);
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
		loopEndControl.layout(this, width - fillH / 2 - margin * 2 - topBarH * 5, 0, topBarH * 5,
				topBarH);
		loopBeginControl.layout(this, loopEndControl.x - topBarH * 5 - margin, 0, topBarH * 5,
				topBarH);

		sampleEdit.layout(this, fillH, topBarH, width - fillH / 2 - fillH - margin * 2, fillH);
	}

	@Override
	public void onFirstPress() {
		loopWindowEvent = new TrackLoopWindowSetEvent(context.getTrackManager().getCurrTrack()
				.getId());
		loopWindowEvent.begin();
	}

	@Override
	public void onLastRelease() {
		loopWindowEvent.end();
	}
}
