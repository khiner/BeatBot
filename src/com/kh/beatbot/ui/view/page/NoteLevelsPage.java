package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.LevelsView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class NoteLevelsPage extends Page {

	private static ShapeGroup labelGroup = new ShapeGroup();
	private LevelsView levelsView;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
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
		volumeToggle.setBgIcon(new RoundedRectIcon(labelGroup, Colors.volumeBgColorSet, Colors.volumeStrokeColorSet));
		panToggle.setBgIcon(new RoundedRectIcon(labelGroup, Colors.panBgColorSet, Colors.panStrokeColorSet));
		pitchToggle.setBgIcon(new RoundedRectIcon(labelGroup, Colors.pitchBgColorSet, Colors.pitchStrokeColorSet));
	}

	@Override
	public void draw() {
		labelGroup.draw(this, 2);
	}

	@Override
	protected void createChildren() {
		levelsView = new LevelsView();
		volumeToggle = new ToggleButton();
		panToggle = new ToggleButton();
		pitchToggle = new ToggleButton();
		
		volumeToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				levelsView.setLevelType(LevelType.VOLUME);
				update();
			}
		});
		panToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				levelsView.setLevelType(LevelType.PAN);
				update();
			}
		});
		pitchToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
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
		float viewW = toggleWidth + 5;
		volumeToggle.layout(this, 0, 0, toggleWidth, toggleHeight);
		panToggle.layout(this, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchToggle.layout(this, 0, toggleHeight * 2, toggleWidth, toggleHeight);
		levelsView.layout(this, viewW, 0, width - viewW, height);
	}
}
