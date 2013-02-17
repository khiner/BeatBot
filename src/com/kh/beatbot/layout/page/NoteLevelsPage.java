package com.kh.beatbot.layout.page;

import android.opengl.GLSurfaceView;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.view.LevelsView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class NoteLevelsPage extends Page {

	private LevelsView levelsView;
	private BBToggleButton volumeToggle, panToggle, pitchToggle;

	
	public NoteLevelsPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		parent.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		// TODO remember to turn off continuous mode when leaving
	}

	public int getRenderMode() {
		// needs continous rendering, since it is linked with the movements
		// of MidiView
		return GLSurfaceView.RENDERMODE_CONTINUOUSLY;
	}
	
	@Override
	public void update() {
		deselectAll();
		selectActiveLevel();
	}

	private void deselectAll() {
		volumeToggle.setOn(false);
		panToggle.setOn(false);
		pitchToggle.setOn(false);
	}

	private void selectActiveLevel() {
		switch (levelsView.getLevelType()) {
		case VOLUME:
			volumeToggle.setOn(true);
			return;
		case PAN:
			panToggle.setOn(true);
			return;
		case PITCH:
			pitchToggle.setOn(true);
			return;
		}
	}

	@Override
	protected void loadIcons() {
		volumeToggle.setIconSource(new BBIconSource(-1, R.drawable.volume_icon, R.drawable.volume_icon_selected));
		panToggle.setIconSource(new BBIconSource(-1, R.drawable.pan_icon, R.drawable.pan_icon_selected));
		pitchToggle.setIconSource(new BBIconSource(-1, R.drawable.pitch_icon, R.drawable.pitch_selected_icon));
	}

	@Override
	public void draw() {
		// parent - no drawing
		
	}

	@Override
	protected void createChildren() {
		levelsView = new LevelsView((TouchableSurfaceView)parent);
		volumeToggle = new BBToggleButton((TouchableSurfaceView)parent);
		panToggle = new BBToggleButton((TouchableSurfaceView)parent);
		pitchToggle = new BBToggleButton((TouchableSurfaceView)parent);
		volumeToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton arg0) {
				levelsView.setLevelType(LevelType.VOLUME);
				update();
			}
		});
		panToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton arg0) {
				levelsView.setLevelType(LevelType.PAN);
				update();
			}
		});
		pitchToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton arg0) {
				levelsView.setLevelType(LevelType.PITCH);
				update();
			}
		});
		addChild(levelsView);
		addChild(volumeToggle);
		addChild(panToggle);
		addChild(pitchToggle);
	}

	@Override
	protected void layoutChildren() {
		float toggleHeight = height / 3;
		float toggleWidth = 2 * toggleHeight;
		volumeToggle.layout(this, 0, 0, toggleWidth, toggleHeight);
		panToggle.layout(this, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchToggle.layout(this, 0, toggleHeight * 2, toggleWidth, toggleHeight);
		levelsView.layout(this, GlobalVars.midiGroup.midiTrackControl.width, 0, width - toggleWidth, height);
	}
}
