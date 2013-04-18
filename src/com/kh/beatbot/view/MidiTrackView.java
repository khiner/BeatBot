package com.kh.beatbot.view;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.TextButton;
import com.kh.beatbot.view.helper.TickWindowHelper;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class MidiTrackView extends TouchableBBView {

	public class ButtonRow extends TouchableBBView {
		int trackNum;
		TextButton instrumentButton, muteButton, soloButton;

		public ButtonRow(int trackNum) {
			this.trackNum = trackNum;
		}

		public void updateInstrumentIcon() {
			instrumentButton.setForegroundIconSource(Managers.trackManager
					.getTrack(trackNum).getInstrument().getIconSource());	
		}
		
		@Override
		protected void loadIcons() {
			updateInstrumentIcon();
			muteButton.setText("M");
			soloButton.setText("S");
		}

		@Override
		public void init() {
			// nothing to do
		}

		@Override
		public void draw() {
			// parent view, no drawing
		}

		@Override
		protected void createChildren() {
			instrumentButton = new TextButton(roundedRectGroup,
					Colors.instrumentBgColorSet, Colors.instrumentStrokeColorSet);
			muteButton = new TextButton(roundedRectGroup,
					Colors.muteButtonColorSet, Colors.labelStrokeColorSet);
			soloButton = new TextButton(roundedRectGroup,
					Colors.soloButtonColorSet, Colors.labelStrokeColorSet);
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
			addChild(instrumentButton);
			addChild(muteButton);
			addChild(soloButton);
		}

		@Override
		public void layoutChildren() {
			instrumentButton.layout(this, 0, 0, height, height);
			muteButton.layout(this, height, 0, height * .72f, height);
			soloButton.layout(this, height + muteButton.width, 0,
					height * .72f, height);
		}
	}

	private static List<ButtonRow> buttonRows = new ArrayList<ButtonRow>();
	private static ShapeGroup roundedRectGroup = new ShapeGroup();
	private float lastY = 0;
	
	protected void loadIcons() {
		Managers.directoryManager.loadIcons();
	}

	public void updateInstrumentIcon(int trackNum) {
		if (trackNum < 0 || trackNum >= buttonRows.size()) {
			return;
		}
		buttonRows.get(trackNum).updateInstrumentIcon();
	}

	public void trackAdded(int trackNum) {
		ButtonRow newRow = new ButtonRow(trackNum);
		buttonRows.add(newRow);
		addChild(newRow);
		if (initialized) {
			layoutChildren();
			newRow.loadAllIcons();
		}
	}

	public void draw() {
		float newY = -absoluteY - TickWindowHelper.getYOffset();
		if (newY != lastY) {
			layoutChildren();
		}
		push();
		translate(-absoluteX, newY);
		roundedRectGroup.draw((GL11)BBView.gl, 1);
		pop();
		lastY = newY; 
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
