package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.IconResourceSets;
import com.kh.beatbot.ui.view.LevelsView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class NoteLevelsPage extends TouchableView {

	private LevelsView levelsView;
	private ToggleButton volumeButton, panButton, pitchButton;

	@Override
	public synchronized void update() {
		deselectAll();
		getActiveLevelButton().setChecked(true);
	}

	private void deselectAll() {
		volumeButton.setChecked(false);
		panButton.setChecked(false);
		pitchButton.setChecked(false);
	}

	private ToggleButton getActiveLevelButton() {
		switch (levelsView.getLevelType()) {
		case VOLUME:
			return volumeButton;
		case PAN:
			return panButton;
		case PITCH:
			return pitchButton;
		default:
			return volumeButton;
		}
	}

	@Override
	protected synchronized void createChildren() {
		levelsView = new LevelsView(shapeGroup);
		volumeButton = new ToggleButton(shapeGroup, true, false);
		panButton = new ToggleButton(shapeGroup, true, false);
		pitchButton = new ToggleButton(shapeGroup, true, false);

		volumeButton.setIcon(IconResourceSets.VOLUME);
		panButton.setIcon(IconResourceSets.PAN);
		pitchButton.setIcon(IconResourceSets.PITCH);

		volumeButton.setText("Vol");
		panButton.setText("Pan");
		pitchButton.setText("Pit");

		volumeButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				levelsView.setLevelType(LevelType.VOLUME);
				update();
			}
		});
		panButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				levelsView.setLevelType(LevelType.PAN);
				update();
			}
		});
		pitchButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				levelsView.setLevelType(LevelType.PITCH);
				update();
			}
		});
		addChildren(levelsView, volumeButton, panButton, pitchButton);
	}

	@Override
	public synchronized void layoutChildren() {
		float toggleHeight = height / 3;
		float toggleWidth = 2 * toggleHeight;
		float viewW = toggleWidth + 5;
		volumeButton.layout(this, 0, 0, toggleWidth, toggleHeight);
		panButton.layout(this, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchButton
				.layout(this, 0, toggleHeight * 2, toggleWidth, toggleHeight);
		levelsView.layout(this, viewW, 0, width - viewW, height);
	}
}
