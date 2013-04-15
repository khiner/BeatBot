package com.kh.beatbot.layout.page;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.LevelsView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.TextButton;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class NoteLevelsPage extends Page {

	private static ShapeGroup labelGroup = new ShapeGroup();
	private LevelsView levelsView;
	private TextButton volumeToggle, panToggle, pitchToggle;
	
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
		volumeToggle.setText("VOL");
		panToggle.setText("PAN");
		pitchToggle.setText("PIT");
	}

	@Override
	public void draw() {
		push();
		translate(-absoluteX, -absoluteY);
		labelGroup.draw((GL11)BBView.gl, 2);
		pop();
	}

	@Override
	protected void createChildren() {
		levelsView = new LevelsView();
		volumeToggle = new TextButton(labelGroup, Colors.volumeBgColorSet, Colors.volumeStrokeColorSet);
		panToggle = new TextButton(labelGroup, Colors.panBgColorSet, Colors.panStrokeColorSet);
		pitchToggle = new TextButton(labelGroup, Colors.pitchBgColorSet, Colors.pitchStrokeColorSet);
		
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
