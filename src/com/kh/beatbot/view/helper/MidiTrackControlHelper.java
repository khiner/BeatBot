package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.TouchableViewWindow;

public class MidiTrackControlHelper extends TouchableViewWindow {

	public class ButtonRow extends TouchableViewWindow {
		int trackNum;
		BBToggleButton instrumentButton, muteButton, soloButton;
		
		public ButtonRow(TouchableSurfaceView parent, int trackNum) {
			super(parent);
			this.trackNum = trackNum;
		}
		
		public void setIconSource(BBIconSource instrumentIcon) {
			instrumentButton.setIconSource(instrumentIcon);
		}
		
		@Override
		protected void loadIcons() {
			// parent loads all button icons
		}

		@Override
		public void init() {
			instrumentButton.setIconSource(Managers.trackManager.getTrack(trackNum).getInstrument().getBBIconSource());
			muteButton.setIconSource(muteIcon);
			soloButton.setIconSource(soloIcon);
			instrumentButton.setOnClickListener(new BBOnClickListener() {
				@Override
				public void onClick(BBButton button) {
					Managers.trackManager.setCurrTrack(trackNum);
					instrumentButton.setOn(true);
					for (ButtonRow buttonRow : buttonRows) {
						if (!buttonRow.instrumentButton.equals(instrumentButton)) {
							buttonRow.instrumentButton.setOn(false);
						}
					}
				}
			});
			muteButton.setOnClickListener(new BBOnClickListener() {
				@Override
				public void onClick(BBButton button) {
					Managers.trackManager.getTrack(trackNum).mute(muteButton.isOn());
				}
			});
			soloButton.setOnClickListener(new BBOnClickListener() {
				@Override
				public void onClick(BBButton button) {
					Managers.trackManager.getTrack(trackNum).solo(soloButton.isOn());
					if (soloButton.isOn()) {
						// if this track is soloing, set all other solo icons to
						// inactive.
						for (ButtonRow buttonRow : buttonRows) {
							if (!buttonRow.soloButton.equals(soloButton)) {
								buttonRow.soloButton.setOn(false);
							}
						}
					}
				}
			});
		}

		@Override
		public void draw() {
			// parent view, no drawing
		}

		@Override
		protected void createChildren() {
			instrumentButton = new BBToggleButton((TouchableSurfaceView)parent);
			muteButton = new BBToggleButton((TouchableSurfaceView)parent);
			soloButton = new BBToggleButton((TouchableSurfaceView)parent);
			addChild(instrumentButton);
			addChild(muteButton);
			addChild(soloButton);
		}

		@Override
		protected void layoutChildren() {
			instrumentButton.layout(this, 0, 0, height, height);
			muteButton.layout(this, height, 0, height * .75f, height);
			soloButton.layout(this, height + muteButton.width, 0, height * .75f, height);
		}
	}

	private static BBIconSource muteIcon, soloIcon;
	private static List<ButtonRow> buttonRows = new ArrayList<ButtonRow>();

	private FloatBuffer bgVb = null;
	
	public MidiTrackControlHelper(TouchableSurfaceView parent) {
		super(parent);
	}
	
	protected void loadIcons() {
		Managers.directoryManager.loadIcons();
		muteIcon = new BBIconSource(-1, R.drawable.mute_icon,
				R.drawable.mute_icon_selected);
		soloIcon = new BBIconSource(-1, R.drawable.solo_icon,
				R.drawable.solo_icon_selected);
	}

	public void updateInstrumentIcon(int trackNum) {
		if (trackNum < 0 || trackNum >= buttonRows.size()) {
			return;
		}
		Track track = Managers.trackManager.getTrack(trackNum);
		buttonRows.get(trackNum).setIconSource(
				track.getInstrument().getBBIconSource());
	}

	public void trackAdded(int trackNum) {
		ButtonRow newRow = new ButtonRow((TouchableSurfaceView)parent, trackNum); 
		buttonRows.add(newRow);
		addChild(newRow);
	}

	public void draw() {
		// draw background fill
		drawTriangleFan(bgVb, Colors.BG_COLOR);
	}

	private void initBgVb() {
		bgVb = makeRectFloatBuffer(0, 0, width, height);
	}
	
	@Override
	public void init() {
		initBgVb();
	}

	@Override
	protected void createChildren() {
		// all button rows are added dynamically as tracks are added
	}

	@Override
	protected void layoutChildren() {
		float yPos = MidiView.Y_OFFSET;
		for (ButtonRow buttonRow : buttonRows) {
			buttonRow.layout(this, 0, yPos, width, MidiView.trackHeight);
			yPos += MidiView.trackHeight;
		}
	}
}
