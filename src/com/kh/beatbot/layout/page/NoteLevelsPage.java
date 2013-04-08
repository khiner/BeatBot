package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.LevelsView;
import com.kh.beatbot.view.ToggleButton;

public class NoteLevelsPage extends Page {

	private LevelsView levelsView;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
	@Override
	public void init() {
		// nothing to do
	}
	
	@Override
	public void update() {
		deselectAll();
		selectActiveLevel();
	}

	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectActiveLevel() {
		switch (levelsView.getLevelType()) {
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

	@Override
	protected void loadIcons() {
		volumeToggle.setIconSource(new ImageIconSource( R.drawable.volume_icon, R.drawable.volume_icon_selected));
		panToggle.setIconSource(new ImageIconSource(R.drawable.pan_icon, R.drawable.pan_icon_selected));
		pitchToggle.setIconSource(new ImageIconSource(R.drawable.pitch_icon, R.drawable.pitch_selected_icon));
	}

	@Override
	public void draw() {
		// parent - no drawing
		
	}

	@Override
	protected void createChildren() {
		levelsView = new LevelsView();
		volumeToggle = new ToggleButton();
		panToggle = new ToggleButton();
		pitchToggle = new ToggleButton();
		volumeToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(Button arg0) {
				levelsView.setLevelType(LevelType.VOLUME);
				update();
			}
		});
		panToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(Button arg0) {
				levelsView.setLevelType(LevelType.PAN);
				update();
			}
		});
		pitchToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(Button arg0) {
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
	public void layoutChildren() {
		float toggleHeight = height / 3;
		float toggleWidth = 2 * toggleHeight;
		volumeToggle.layout(this, 0, 0, toggleWidth, toggleHeight);
		panToggle.layout(this, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchToggle.layout(this, 0, toggleHeight * 2, toggleWidth, toggleHeight);
		levelsView.layout(this, GlobalVars.mainPage.midiTrackControl.width,
				0, width - toggleWidth, height);
	}
}
