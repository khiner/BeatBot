package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.LevelsView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class NoteLevelsPage extends TouchableView {

	private LevelsView levelsView;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
	@Override
	public synchronized void update() {
		shouldClip = false;
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
	protected synchronized void initIcons() {
		volumeToggle.setText("Vol");
		panToggle.setText("Pan");
		pitchToggle.setText("Pit");
		volumeToggle.setBgIcon(new RoundedRectIcon(shapeGroup, Colors.volumeFillColorSet, Colors.volumeStrokeColorSet));
		panToggle.setBgIcon(new RoundedRectIcon(shapeGroup, Colors.panFillColorSet, Colors.panStrokeColorSet));
		pitchToggle.setBgIcon(new RoundedRectIcon(shapeGroup, Colors.pitchFillColorSet, Colors.pitchStrokeColorSet));
	}

	@Override
	public void draw() {
		shapeGroup.draw();
	}

	public synchronized void drawChildren() {
		super.drawChildren();
	}

	@Override
	protected synchronized void createChildren() {
		levelsView = new LevelsView(shapeGroup);
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
		addChildren(levelsView, volumeToggle, panToggle, pitchToggle);
	}

	@Override
	public synchronized void layoutChildren() {
		float toggleHeight = height / 3;
		float toggleWidth = 2 * toggleHeight;
		float viewW = toggleWidth + 5;
		volumeToggle.layout(null, 0, 0, toggleWidth, toggleHeight);
		panToggle.layout(null, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchToggle.layout(null, 0, toggleHeight * 2, toggleWidth, toggleHeight);
		levelsView.layout(this, viewW, 0, width - viewW, height);
	}
}
