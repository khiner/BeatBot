package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronSeekbar;

public class LevelsPage extends TrackPage implements LevelListener {
	private enum LevelType {VOLUME, PAN, PITCH};
	private TronSeekbar trackLevel;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	private LevelType activeLevelType = LevelType.VOLUME;
	
	public LevelsPage(Context context) {
		super(context);
	}

	@Override
	protected void inflate(Context context) {
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.levels_edit, this);
        trackLevel = (TronSeekbar) view.findViewById(R.id.trackLevel);
		trackLevel.addLevelListener(this);
		volumeToggle = (ToggleButton) view.findViewById(R.id.trackVolumeToggle);
		panToggle = (ToggleButton) view.findViewById(R.id.trackPanToggle);
		pitchToggle = (ToggleButton) view.findViewById(R.id.trackPitchToggle);
		
		volumeToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	activeLevelType = LevelType.VOLUME;
            	panToggle.setChecked(false);
            	pitchToggle.setChecked(false);
            	updateDisplay();
            }
        });
		panToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	activeLevelType = LevelType.PAN;
            	volumeToggle.setChecked(false);
            	pitchToggle.setChecked(false);
            	updateDisplay();
            }
        });
		pitchToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	activeLevelType = LevelType.PITCH;
            	panToggle.setChecked(false);
            	volumeToggle.setChecked(false);
            	updateDisplay();
            }
        });
	}

	@Override
	protected void trackUpdated() {
		updateDisplay();
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		switch (activeLevelType) {
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
		track.setPrimaryVolume(level);
	}

	@Override
	public void notifyInit(LevelListenable levelBar) {
		updateDisplay();
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
	
	private float[] getActiveLevelColor() {
		switch (activeLevelType) {
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
		switch (activeLevelType) {
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
		trackLevel.setLevelColor(getActiveLevelColor());
		trackLevel.setLevel(getActiveLevel());
	}
}
