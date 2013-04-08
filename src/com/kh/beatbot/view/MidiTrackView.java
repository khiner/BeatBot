package com.kh.beatbot.view;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.IconSource;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.helper.TickWindowHelper;

public class MidiTrackView extends TouchableBBView {

	public class ButtonRow extends TouchableBBView {
		int trackNum;
		ToggleButton instrumentButton;
		TextButton muteButton, soloButton;

		public ButtonRow(int trackNum) {
			this.trackNum = trackNum;
		}

		public void setIconSource(IconSource instrumentIcon) {
			instrumentButton.setIconSource(instrumentIcon);
		}

		@Override
		protected void loadIcons() {
			// parent loads all button icons
		}

		@Override
		public void init() {
			instrumentButton.setIconSource(Managers.trackManager
					.getTrack(trackNum).getInstrument().getIconSource());
			muteButton.setText("M");
			soloButton.setText("S");
			instrumentButton.setOnClickListener(new BBOnClickListener() {
				@Override
				public void onClick(Button button) {
					instrumentButton.setChecked(true);
					for (ButtonRow buttonRow : buttonRows) {
						if (!buttonRow.instrumentButton
								.equals(instrumentButton)) {
							buttonRow.instrumentButton.setChecked(false);
						}
					}
					Managers.trackManager.setCurrTrack(trackNum);
				}
			});
			muteButton.setOnClickListener(new BBOnClickListener() {
				@Override
				public void onClick(Button button) {
					Managers.trackManager.getTrack(trackNum).mute(
							muteButton.isChecked());
				}
			});
			soloButton.setOnClickListener(new BBOnClickListener() {
				@Override
				public void onClick(Button button) {
					if (soloButton.isChecked()) {
						// if this track is soloing, set all other solo icons to
						// inactive.
						for (ButtonRow buttonRow : buttonRows) {
							if (!buttonRow.soloButton.equals(soloButton)) {
								buttonRow.soloButton.setChecked(false);
							}
						}
					}
					Managers.trackManager.getTrack(trackNum).solo(
							soloButton.isChecked());
				}
			});
		}

		@Override
		public void draw() {
			// parent view, no drawing
		}

		@Override
		protected void createChildren() {
			instrumentButton = new ToggleButton();
			muteButton = new TextButton(GlobalVars.mainPage.roundedRectGroup,
					Colors.muteButtonColorSet, Colors.labelStrokeColorSet);
			soloButton = new TextButton(GlobalVars.mainPage.roundedRectGroup,
					Colors.soloButtonColorSet, Colors.labelStrokeColorSet);
			addChild(instrumentButton);
			addChild(muteButton);
			addChild(soloButton);
		}

		@Override
		public void layoutChildren() {
			instrumentButton.layout(this, 0, 0, height, height);
			muteButton.layout(this, height, 0, height * .75f, height);
			soloButton.layout(this, height + muteButton.width, 0,
					height * .75f, height);
		}
	}

	private static List<ButtonRow> buttonRows = new ArrayList<ButtonRow>();

	protected void loadIcons() {
		Managers.directoryManager.loadIcons();
	}

	public void updateInstrumentIcon(int trackNum) {
		if (trackNum < 0 || trackNum >= buttonRows.size()) {
			return;
		}
		Track track = Managers.trackManager.getTrack(trackNum);
		buttonRows.get(trackNum).setIconSource(
				track.getInstrument().getIconSource());
	}

	public void trackAdded(int trackNum) {
		ButtonRow newRow = new ButtonRow(trackNum);
		buttonRows.add(newRow);
		addChild(newRow);
	}

	public void draw() {
		// parent
	}

	@Override
	public void init() {
		// nothing
	}

	@Override
	protected void createChildren() {
		// all button rows are added dynamically as tracks are added
	}

	@Override
	public void layoutChildren() {
		float yPos = MidiView.Y_OFFSET - TickWindowHelper.getYOffset();
		for (ButtonRow buttonRow : buttonRows) {
			buttonRow.layout(this, 0, yPos, width, MidiView.trackHeight);
			yPos += MidiView.trackHeight;
		}
	}
}
