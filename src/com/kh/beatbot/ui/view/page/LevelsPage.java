package com.kh.beatbot.ui.view.page;

import java.util.concurrent.atomic.AtomicInteger;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.event.TrackLevelsSetEvent;
import com.kh.beatbot.listener.TouchableViewListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Seekbar.BasePosition;
import com.kh.beatbot.ui.view.control.ValueLabel;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl.SeekbarPosition;

public class LevelsPage extends TrackPage implements TouchableViewListener {
	// levels attrs
	protected SeekbarParamControl volumeParamControl, panParamControl;
	protected PitchParamControl pitchParamControl;
	protected boolean masterMode = false;

	public LevelsPage(View view) {
		super(view);
	}

	@Override
	public void onSelect(BaseTrack track) {
		volumeParamControl.setParam(getCurrTrack().getVolumeParam());
		panParamControl.setParam(getCurrTrack().getPanParam());
		pitchParamControl.setParams(getCurrTrack().getPitchParam(), getCurrTrack()
				.getPitchCentParam());
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
		pitchParamControl = new PitchParamControl(this);

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
			levelsSetEvent = new TrackLevelsSetEvent(getCurrTrack().getId());
			levelsSetEvent.begin();
		}
	}

	@Override
	public void onRelease(TouchableView view) {
		if (numControlsPressed.decrementAndGet() == 0) {
			levelsSetEvent.end();
		}
	}

	// Pitch has two params/value-labels that can be switched between (steps/cents)
	// The last value-label to be touched is what the seekbar controls
	private class PitchParamControl extends SeekbarParamControl {
		private ValueLabel centValueLabel, currentValueLabel;

		public PitchParamControl(View view) {
			super(view, SeekbarPosition.CENTER, BasePosition.CENTER);
		}

		@Override
		public synchronized void createChildren() {
			super.createChildren();
			centValueLabel = new ValueLabel(this);
			centValueLabel.setShrinkable(true);
			centValueLabel.setListener(this);
			label.setIcon(IconResourceSets.PITCH);
			currentValueLabel = valueLabel;
		}

		@Override
		public void setId(int id) {
			super.setId(id);
			centValueLabel.setId(id);
		}

		@Override
		public void onPress(TouchableView view) {
			label.press();
			if (view instanceof ValueLabel && !currentValueLabel.equals(view)) {
				currentValueLabel = (ValueLabel) view;
				levelControl.setParam(currentValueLabel.getParam());
			}
			currentValueLabel.press();
			levelControl.press();
			if (null != touchListener) {
				touchListener.onPress(view);
			}
		}

		@Override
		public void onRelease(TouchableView view) {
			label.release();
			currentValueLabel.release();
			levelControl.release();
			if (null != touchListener) {
				touchListener.onRelease(view);
			}
		}

		@Override
		public synchronized void layoutChildren() {
			label.layout(this, 0, 0, height * 2, height);
			levelControl.layout(this, height * 2, 0, width - height * 6, height);
			valueLabel.layout(this, width - height * 4, 0, height * 2, height);
			centValueLabel.layout(this, width - height * 2, 0, height * 2, height);
		}

		public void setParams(Param stepParam, Param centParam) {
			label.setText(stepParam.getName());
			valueLabel.setParam(stepParam);
			centValueLabel.setParam(centParam);
			levelControl.setParam(currentValueLabel.getParam());
		}
	}
}
