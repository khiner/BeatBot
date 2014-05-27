package com.kh.beatbot.ui.view.page;

import java.util.concurrent.atomic.AtomicInteger;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.event.TrackLevelsSetEvent;
import com.kh.beatbot.listener.ControlViewListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ControlViewBase;
import com.kh.beatbot.ui.view.control.Seekbar;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class LevelsPage extends TrackPage implements ControlViewListener {
	// levels attrs
	protected Seekbar volumeLevelBar, panLevelBar, pitchLevelBar;
	protected Button volumeButton, panButton, pitchButton;
	protected boolean masterMode = false;

	public LevelsPage(View view) {
		super(view);
	}

	@Override
	public void onSelect(BaseTrack track) {
		volumeLevelBar.setParam(getCurrTrack().getVolumeParam());
		panLevelBar.setParam(getCurrTrack().getPanParam());
		pitchLevelBar.setParam(getCurrTrack().getPitchParam());
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}

	public BaseTrack getCurrTrack() {
		return masterMode ? TrackManager.getMasterTrack() : TrackManager.currTrack;
	}

	@Override
	protected synchronized void createChildren() {
		volumeButton = new ToggleButton(this).withRoundedRect().withIcon(IconResourceSets.VOLUME);
		panButton = new ToggleButton(this).withRoundedRect().withIcon(IconResourceSets.PAN);
		pitchButton = new ToggleButton(this).withRoundedRect().withIcon(IconResourceSets.PITCH);

		volumeLevelBar = new Seekbar(this);
		panLevelBar = new Seekbar(this);
		pitchLevelBar = new Seekbar(this);

		volumeButton.setText("Vol");
		panButton.setText("Pan");
		pitchButton.setText("Pit");

		volumeButton.setEnabled(false);
		panButton.setEnabled(false);
		pitchButton.setEnabled(false);

		volumeLevelBar.setLevelColor(Color.TRON_BLUE, Color.TRON_BLUE_TRANS);
		panLevelBar.setLevelColor(Color.PAN, Color.PAN_TRANS);
		pitchLevelBar.setLevelColor(Color.PITCH, Color.PITCH_TRANS);

		volumeLevelBar.setListener(this);
		panLevelBar.setListener(this);
		pitchLevelBar.setListener(this);
	}

	@Override
	public synchronized void layoutChildren() {

		float toggleHeight = height / 3;
		float toggleWidth = 2 * toggleHeight;
		volumeButton.layout(this, 0, 0, toggleWidth, toggleHeight);
		panButton.layout(this, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchButton.layout(this, 0, toggleHeight * 2, toggleWidth, toggleHeight);

		volumeLevelBar.layout(this, toggleWidth, 0, width - toggleWidth, toggleHeight);
		panLevelBar.layout(this, toggleWidth, toggleHeight, width - toggleWidth, toggleHeight);
		pitchLevelBar
				.layout(this, toggleWidth, toggleHeight * 2, width - toggleWidth, toggleHeight);
	}

	private AtomicInteger numControlsPressed = new AtomicInteger(0);
	private TrackLevelsSetEvent levelsSetEvent = null;

	@Override
	public void onPress(ControlViewBase control) {
		if (numControlsPressed.getAndIncrement() == 0) {
			levelsSetEvent = new TrackLevelsSetEvent(getCurrTrack());
			levelsSetEvent.begin();
		}
		getButton((Seekbar) control).press();
	}

	@Override
	public void onRelease(ControlViewBase control) {
		if (numControlsPressed.decrementAndGet() == 0) {
			levelsSetEvent.end();
		}
		getButton((Seekbar) control).disable();
	}

	private Button getButton(Seekbar seekbar) {
		if (seekbar.equals(volumeLevelBar)) {
			return volumeButton;
		} else if (seekbar.equals(panLevelBar)) {
			return panButton;
		} else {
			return pitchButton;
		}
	}
}
