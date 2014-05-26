package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.NoteLevelsView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class NoteLevelsPage extends TrackPage {

	private NoteLevelsView noteLevelsView;
	private ToggleButton volumeButton, panButton, pitchButton;

	public NoteLevelsPage(View view) {
		super(view);
	}

	@Override
	public void onSelect(BaseTrack track) {
		deselectAll();
		getActiveLevelButton().setChecked(true);
	}

	private void deselectAll() {
		volumeButton.setChecked(false);
		panButton.setChecked(false);
		pitchButton.setChecked(false);
	}

	private ToggleButton getActiveLevelButton() {
		switch (noteLevelsView.getLevelType()) {
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
		noteLevelsView = new NoteLevelsView(this);
		volumeButton = new ToggleButton(this).withRoundedRect().withIcon(IconResourceSets.VOLUME);
		panButton = new ToggleButton(this).withRoundedRect().withIcon(IconResourceSets.PAN);
		pitchButton = new ToggleButton(this).withRoundedRect().withIcon(IconResourceSets.PITCH);

		volumeButton.setText("Vol");
		panButton.setText("Pan");
		pitchButton.setText("Pit");

		volumeButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				noteLevelsView.setLevelType(LevelType.VOLUME);
				onSelect(null);
			}
		});
		panButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				noteLevelsView.setLevelType(LevelType.PAN);
				onSelect(null);
			}
		});
		pitchButton.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button arg0) {
				noteLevelsView.setLevelType(LevelType.PITCH);
				onSelect(null);
			}
		});
	}

	@Override
	public synchronized void layoutChildren() {
		float toggleHeight = height / 3;
		float toggleWidth = 2 * toggleHeight;
		volumeButton.layout(this, 0, 0, toggleWidth, toggleHeight);
		panButton.layout(this, 0, toggleHeight, toggleWidth, toggleHeight);
		pitchButton.layout(this, 0, toggleHeight * 2, toggleWidth, toggleHeight);
		noteLevelsView.layout(this, toggleWidth + BG_OFFSET, BG_OFFSET, width - toggleWidth
				- BG_OFFSET * 2, height - BG_OFFSET * 2);
	}
}
