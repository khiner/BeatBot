package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.MotionEvent;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.GLSurfaceViewBase;
import com.kh.beatbot.view.MidiView;

public class MidiTrackControlHelper {
	public static class ButtonRow {
		int trackNum;
		float width;
		BBToggleButton instrumentButton, muteButton, soloButton;

		public ButtonRow(int trackNum, BBIconSource instrumentIcon) {
			this.trackNum = trackNum;
			instrumentButton = new BBToggleButton(instrumentIcon,
					MidiView.trackHeight);
			muteButton = new BBToggleButton(muteIcon, MidiView.trackHeight);
			soloButton = new BBToggleButton(soloIcon, MidiView.trackHeight);
			width = instrumentButton.getWidth() + muteButton.getWidth()
					+ soloButton.getWidth();
		}

		public void setIconSource(BBIconSource instrumentIcon) {
			instrumentButton.setIconSource(instrumentIcon);
		}

		public void draw(float y) {
			instrumentButton.draw(0, y);
			muteButton.draw(instrumentButton.getWidth(), y);
			soloButton.draw(
					instrumentButton.getWidth() + muteButton.getWidth(), y);
		}

		public void handlePress(float x) {
			BBButton pressedButton = getButton(x);
			if (pressedButton != null) {
				pressedButton.touch();
			}
		}

		public void handleMove(float x) {
			BBButton currentSelected = getSelectedButton();
			if (currentSelected != null
					&& !currentSelected.equals(getButton(x))) {
				currentSelected.release();
			}
		}

		public void handleLongPress(float x) {
			BBToggleButton pressedButton = getButton(x);
			if (pressedButton.equals(instrumentButton)) {
				listener.trackLongPressed(trackNum);
			}
			//instrumentButton.release();
		}

		public void handleRelease(float x) {
			BBToggleButton releasedButton = getButton(x);
			if (releasedButton != null) {
				if (releasedButton.equals(instrumentButton) && !instrumentButton.isOn()) {
					releasedButton.toggle();
					listener.trackClicked(trackNum);
					for (ButtonRow buttonRow : buttonRows) {
						if (!buttonRow.equals(this)) {
							buttonRow.instrumentButton.setOn(false);
						}
					}
				} else if (releasedButton.equals(muteButton)) {
					releasedButton.toggle();
					listener.muteToggled(trackNum, releasedButton.isOn());
				} else if (releasedButton.equals(soloButton)) {
					releasedButton.toggle();
					listener.soloToggled(trackNum, releasedButton.isOn());
					if (soloButton.isOn()) {
						// if this track is soloing, set all other solo icons to
						// inactive.
						for (ButtonRow buttonRow : buttonRows) {
							if (!buttonRow.equals(this)) {
								buttonRow.soloButton.setOn(false);
							}
						}
					}
				}
			}
			releaseAll();
		}

		public void releaseAll() {
			instrumentButton.release();
			muteButton.release();
			soloButton.release();
		}

		private BBToggleButton getButton(float x) {
			if (x < buttonRows.get(0).instrumentButton.getWidth()) {
				return instrumentButton;
			} else if (x < buttonRows.get(0).instrumentButton.getWidth()
					+ buttonRows.get(0).muteButton.getWidth()) {
				return muteButton;
			} else if (x < width) {
				return soloButton;
			} else {
				return null;
			}
		}

		private BBButton getSelectedButton() {
			if (instrumentButton.isTouched()) {
				return instrumentButton;
			} else if (muteButton.isTouched()) {
				return muteButton;
			} else if (soloButton.isTouched()) {
				return soloButton;
			} else {
				return null;
			}
		}
	}

	public static final int NUM_CONTROLS = 3; // mute/solo/track settings

	private static BBIconSource muteIcon, soloIcon;
	private static MidiView midiView = null;
	private static MidiTrackControlListener listener;
	private static FloatBuffer bgRectVb = null;
	private static List<ButtonRow> buttonRows = new ArrayList<ButtonRow>();
	private static Map<Integer, ButtonRow> whichRowOwnsPointer = new HashMap<Integer, ButtonRow>();

	
	public static void loadIcons() {
		muteIcon = new BBIconSource(-1, R.drawable.mute_icon,
				R.drawable.mute_icon_selected);
		soloIcon = new BBIconSource(-1, R.drawable.solo_icon,
				R.drawable.solo_icon_selected);
	}
	
	public static void init(MidiView _midiView) {
		if (!buttonRows.isEmpty()) {
			// it is possible that this static class can be reinstantiated after
			// switching between views.
			// ensure we're not re-adding rows by exiting
			return;
		}
		midiView = _midiView;
		MidiView.allTracksHeight = midiView.getMidiHeight();
		MidiView.trackHeight = midiView.getMidiHeight()
				/ Managers.trackManager.getNumTracks();
		for (int i = 0; i < Managers.trackManager.getNumTracks(); i++) {
			buttonRows.add(new ButtonRow(i, Managers.trackManager.getTrack(i)
					.getInstrument().getBBIconSource()));
		}
		MidiView.X_OFFSET = buttonRows.get(0).width;
		initBgRectVb();
	}

	public static void updateInstrumentIcon(int trackNum) {
		if (trackNum < 0 || trackNum >= buttonRows.size()) {
			return;
		}
		Track track = Managers.trackManager.getTrack(trackNum);
		buttonRows.get(trackNum).setIconSource(
				track.getInstrument().getBBIconSource());
	}

	public static void addListener(MidiTrackControlListener listener) {
		MidiTrackControlHelper.listener = listener;
	}

	public static void addTrack(int trackNum, BBIconSource instrumentIcon) {
		buttonRows.add(new ButtonRow(trackNum, instrumentIcon));
		MidiView.allTracksHeight = MidiView.trackHeight * buttonRows.size();
		initBgRectVb();
	}

	/** draw background color & track control icons */
	public static void draw() {
		GLSurfaceViewBase.drawTriangleStrip(bgRectVb, Colors.BG_COLOR);
		float y = midiView.getMidiHeight() - MidiView.trackHeight
				+ TickWindowHelper.getYOffset();
		for (int i = 0; i < buttonRows.size(); i++) {
			// avoid concurrent modification exception
			ButtonRow buttonRow = buttonRows.get(i);
			buttonRow.draw(y);
			y -= MidiView.trackHeight;
		}
	}

	public static boolean ownsPointer(int pointerId) {
		return whichRowOwnsPointer.containsKey(pointerId);
	}

	public static int getNumPointersDown() {
		return whichRowOwnsPointer.size();
	}

	public static void clearPointers() {
		whichRowOwnsPointer.clear();
	}

	public static void handlePress(int id, float x, int track) {
		if (track < 0 || track >= buttonRows.size()) {
			return;
		}
		ButtonRow selectedRow = buttonRows.get(track);
		whichRowOwnsPointer.put(id, selectedRow);
		selectedRow.handlePress(x);
	}

	public static void handleMove(MotionEvent e, int id, float x, float y) {
		for (int i = 0; i < e.getPointerCount(); i++) {
			id = e.getPointerId(i);
			if (ownsPointer(id)) {
				handleMove(id, e.getX(id), MidiView.yToNote(e.getY(id)));
			}
		}
	}

	public static void handleMove(int id, float x, int track) {
		if (track < 0 || track >= buttonRows.size()) {
			return;
		}
		ButtonRow selectedRow = buttonRows.get(track);
		if (selectedRow.equals(whichRowOwnsPointer.get(id))) {
			selectedRow.handleMove(x);
		} else {
			whichRowOwnsPointer.get(id).releaseAll();
		}
	}

	public static void handleLongPress(int id, float x, int track) {
		ButtonRow selectedRow = whichRowOwnsPointer.get(id);
		if (selectedRow != null) {
			selectedRow.handleLongPress(x);
		}
	}

	public static void handleRelease(int id, float x, int track) {
		ButtonRow selectedRow = whichRowOwnsPointer.get(id);
		if (selectedRow != null && track >= 0 && track < buttonRows.size()) {
			if (selectedRow.equals(buttonRows.get(track))) {
				selectedRow.handleRelease(x);
			}
			selectedRow.releaseAll();
		}
		whichRowOwnsPointer.remove(id);
	}

	private static void initBgRectVb() {
		bgRectVb = GLSurfaceViewBase.makeRectFloatBuffer(0, 0, MidiView.X_OFFSET,
				midiView.getHeight());
	}
}
