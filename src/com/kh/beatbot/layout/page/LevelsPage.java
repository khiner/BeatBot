package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.BBSeekbar;

public class LevelsPage extends Page implements LevelListener {
	private BaseTrack currTrack = null;
	private BBSeekbar trackLevel;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
	public LevelsPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		currTrack = TrackManager.currTrack;
		trackLevel = (BBSeekbar) findViewById(R.id.trackLevel);
		trackLevel.addLevelListener(this);
		volumeToggle = (ToggleButton) findViewById(R.id.trackVolumeToggle);
		panToggle = (ToggleButton) findViewById(R.id.trackPanToggle);
		pitchToggle = (ToggleButton) findViewById(R.id.trackPitchToggle);
		volumeToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				currTrack.activeLevelType = LevelType.VOLUME;
				update();
			}
		});
		panToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				currTrack.activeLevelType = LevelType.PAN;
				update();
			}
		});
		pitchToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				currTrack.activeLevelType = LevelType.PITCH;
				update();
			}
		});
	}
	
	@Override
	public void update() {
		deselectAll();
		selectActiveLevel();
		trackLevel.setLevelColor(getActiveLevelColor());
		trackLevel.setLevel(getActiveLevel());
	}

	@Override
	public void setVisibilityCode(int code) {
		if (code == View.VISIBLE) {
			trackLevel.onResume();
		} else {
			trackLevel.onPause();
		}
		trackLevel.setVisibility(code);
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			currTrack.setVolume(level);
			break;
		case PAN:
			currTrack.setPan(level);
			break;
		case PITCH:
			currTrack.setPitch(level);
			break;
		}
	}

	public void setMasterMode(boolean masterMode) {
		currTrack = masterMode ? TrackManager.masterTrack : TrackManager.currTrack;
	}
	
	@Override
	public void notifyInit(LevelListenable levelBar) {
		// do nothing when levelbar initialized
	}

	@Override
	public void notifyPressed(LevelListenable levelBar, boolean pressed) {
		// do nothing when level pressed
	}

	@Override
	public void notifyClicked(LevelListenable levelListenable) {
		// do nothing when levels are clicked
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// for 2d seekbar. nothing to do
	}

	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectActiveLevel() {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			volumeToggle.setChecked(true);
			return;
		case PAN:
			panToggle.setChecked(true);
			return;
		case PITCH:
			pitchToggle.setChecked(true);
			return;
		}
	}

	private float[] getActiveLevelColor() {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			return Colors.VOLUME;
		case PAN:
			return Colors.PAN;
		case PITCH:
			return Colors.PITCH;
		}
		return Colors.VOLUME;
	}

	private float getActiveLevel() {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			return currTrack.volume;
		case PAN:
			return currTrack.pan;
		case PITCH:
			return currTrack.pitch;
		}
		return currTrack.volume;
	}
}
