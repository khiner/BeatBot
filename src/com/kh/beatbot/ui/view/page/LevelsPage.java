package com.kh.beatbot.ui.view.page;

import java.util.concurrent.atomic.AtomicInteger;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.event.TrackLevelsSetEvent;
import com.kh.beatbot.listener.TouchableViewListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Seekbar.BasePosition;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl.SeekbarPosition;

public class LevelsPage extends TrackPage implements TouchableViewListener {
	// levels attrs
	protected SeekbarParamControl volumeParamControl, panParamControl, pitchParamControl;
	protected boolean masterMode = false;

	public LevelsPage(View view) {
		super(view);
	}

	@Override
	public void onSelect(BaseTrack track) {
		volumeParamControl.setParam(getCurrTrack().getVolumeParam());
		panParamControl.setParam(getCurrTrack().getPanParam());
		pitchParamControl.setParam(getCurrTrack().getPitchParam());
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}

	public BaseTrack getCurrTrack() {
		return masterMode ? TrackManager.getMasterTrack() : TrackManager.currTrack;
	}

	@Override
	protected synchronized void createChildren() {
		volumeParamControl = new SeekbarParamControl(this, SeekbarPosition.CENTER,
				BasePosition.LEFT).withLabelIcon(IconResourceSets.VOLUME);
		panParamControl = new SeekbarParamControl(this, SeekbarPosition.CENTER, BasePosition.CENTER)
				.withLabelIcon(IconResourceSets.PAN);
		pitchParamControl = new SeekbarParamControl(this, SeekbarPosition.CENTER,
				BasePosition.CENTER).withLabelIcon(IconResourceSets.PITCH);

		volumeParamControl.setLevelColor(Color.TRON_BLUE, Color.TRON_BLUE_TRANS);
		panParamControl.setLevelColor(Color.PAN, Color.PAN_TRANS);
		pitchParamControl.setLevelColor(Color.PITCH, Color.PITCH_TRANS);

		volumeParamControl.setTouchListener(this);
		panParamControl.setTouchListener(this);
		pitchParamControl.setTouchListener(this);
	}

	@Override
	public synchronized void layoutChildren() {
		float toggleHeight = height / 3;
		volumeParamControl.layout(this, 0, 0, width, toggleHeight);
		panParamControl.layout(this, 0, toggleHeight, width, toggleHeight);
		pitchParamControl.layout(this, 0, toggleHeight * 2, width, toggleHeight);
	}

	private AtomicInteger numControlsPressed = new AtomicInteger(0);
	private TrackLevelsSetEvent levelsSetEvent = null;

	@Override
	public void onPress(TouchableView view) {
		if (numControlsPressed.getAndIncrement() == 0) {
			levelsSetEvent = new TrackLevelsSetEvent(getCurrTrack());
			levelsSetEvent.begin();
		}
	}

	@Override
	public void onRelease(TouchableView view) {
		if (numControlsPressed.decrementAndGet() == 0) {
			levelsSetEvent.end();
		}
	}
}
