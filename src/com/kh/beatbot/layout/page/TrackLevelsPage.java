package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.BBSeekbar;

public class TrackLevelsPage extends Page implements LevelListener {
	
	public TrackLevelsPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private BBSeekbar trackLevel;
	private ToggleButton volumeToggle, panToggle, pitchToggle;

	public void init() {
		trackLevel = (BBSeekbar) findViewById(R.id.trackLevel);
		trackLevel.addLevelListener(this);
		volumeToggle = (ToggleButton) findViewById(R.id.trackVolumeToggle);
		panToggle = (ToggleButton) findViewById(R.id.trackPanToggle);
		pitchToggle = (ToggleButton) findViewById(R.id.trackPitchToggle);

		volumeToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				GlobalVars.currTrack.activeLevelType = LevelType.VOLUME;
				update();
			}
		});
		panToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				GlobalVars.currTrack.activeLevelType = LevelType.PAN;
				update();
			}
		});
		pitchToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				GlobalVars.currTrack.activeLevelType = LevelType.PITCH;
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
		trackLevel.setVisibility(code);
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		switch (GlobalVars.currTrack.activeLevelType) {
		case VOLUME:
			GlobalVars.currTrack.setPrimaryVolume(level);
			break;
		case PAN:
			GlobalVars.currTrack.setPrimaryPan(level);
			break;
		case PITCH:
			GlobalVars.currTrack.setPrimaryPitch(level);
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
		switch (GlobalVars.currTrack.activeLevelType) {
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
		switch (GlobalVars.currTrack.activeLevelType) {
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
		switch (GlobalVars.currTrack.activeLevelType) {
		case VOLUME:
			return GlobalVars.currTrack.volume;
		case PAN:
			return GlobalVars.currTrack.pan;
		case PITCH:
			return GlobalVars.currTrack.pitch;
		}
		return GlobalVars.currTrack.volume;
	}
}
