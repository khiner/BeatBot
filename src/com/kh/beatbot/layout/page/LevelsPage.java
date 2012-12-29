package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.BBSeekbar;

public class LevelsPage extends TrackPage implements LevelListener {
	private BBSeekbar trackLevel;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
	public LevelsPage(Context context, View layout) {
		super(context, layout);
        trackLevel = (BBSeekbar) layout.findViewById(R.id.trackLevel);
		trackLevel.addLevelListener(this);
		volumeToggle = (ToggleButton) layout.findViewById(R.id.trackVolumeToggle);
		panToggle = (ToggleButton) layout.findViewById(R.id.trackPanToggle);
		pitchToggle = (ToggleButton) layout.findViewById(R.id.trackPitchToggle);
		
		volumeToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	track.activeLevelType = LevelType.VOLUME;
            	updateDisplay();
            }
        });
		panToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	track.activeLevelType = LevelType.PAN;
            	updateDisplay();
            }
        });
		pitchToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	track.activeLevelType = LevelType.PITCH;
            	updateDisplay();
            }
        });
	}

	@Override
	protected void update() {
		updateDisplay();
	}

	@Override
	public void setVisibilityCode(int code) {
		trackLevel.setVisibility(code);
	}
	
	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		switch (track.activeLevelType) {
		case VOLUME:
			track.setPrimaryVolume(level);
			break;
		case PAN:
			track.setPrimaryPan(level);
			break;
		case PITCH:
			track.setPrimaryPitch(level);
			break;
		}
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
		switch (track.activeLevelType) {
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
		switch (track.activeLevelType) {
		case VOLUME:
			return Colors.VOLUME_COLOR;
		case PAN:
			return Colors.PAN_COLOR;
		case PITCH:
			return Colors.PITCH_COLOR;
		}
		return Colors.VOLUME_COLOR;
	}
	
	private float getActiveLevel() {
		switch (track.activeLevelType) {
		case VOLUME:
			return track.volume;
		case PAN:
			return track.pan;
		case PITCH:
			return track.pitch;
		}
		return track.volume;
	}
	
	private void updateDisplay() {
		deselectAll();
		selectActiveLevel();
		trackLevel.setLevelColor(getActiveLevelColor());
		trackLevel.setLevel(getActiveLevel());
	}
}
