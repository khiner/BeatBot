package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.MotionEvent;

import com.kh.beatbot.global.BeatBotButton;
import com.kh.beatbot.global.BeatBotIconSource;
import com.kh.beatbot.global.BeatBotToggleButton;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;

public class MidiTrackControlHelper {
	public static class ButtonRow {
		int trackNum;
		float width;
		BeatBotButton instrumentButton;
		BeatBotToggleButton muteButton, soloButton;

		public ButtonRow(int trackNum, BeatBotIconSource instrumentIcon) {
			this.trackNum = trackNum;
			this.instrumentButton = new BeatBotButton(instrumentIcon);

			muteButton = new BeatBotToggleButton(GlobalVars.muteIcon);
			soloButton = new BeatBotToggleButton(GlobalVars.soloIcon);
			width = instrumentButton.getIconWidth() + muteButton.getIconWidth()
					+ soloButton.getIconWidth();
		}

		public void setIconSource(BeatBotIconSource instrumentIcon) {
			this.instrumentButton.setIconSource(instrumentIcon);
		}
		
		public void draw(float y) {
			instrumentButton.draw(0, y);
			muteButton.draw(instrumentButton.getIconWidth(), y);
			soloButton
					.draw(instrumentButton.getIconWidth()
							+ muteButton.getIconWidth(), y);
		}

		public void handlePress(float x) {
			BeatBotButton pressedButton = getButton(x);
			if (pressedButton != null) {
				pressedButton.touch();
			}
		}

		public void handleMove(float x) {
			BeatBotButton currentSelected = getSelectedButton();
			if (currentSelected != null
					&& !currentSelected.equals(getButton(x))) {
				currentSelected.release();
			}
		}

		public void handleLongPress(float x) {
			BeatBotButton pressedButton = getButton(x);
			if (pressedButton.equals(instrumentButton)
					&& pressedButton.isTouched()) {
				listener.trackLongPressed(trackNum);
			}
			instrumentButton.release();
		}

		public void handleRelease(float x) {
			BeatBotButton releasedButton = getButton(x);
			if (releasedButton != null) {
				if (releasedButton.equals(instrumentButton)
						&& releasedButton.isTouched()) {
					listener.trackClicked(trackNum);
				} else if (releasedButton.equals(muteButton)) {
					((BeatBotToggleButton) releasedButton).toggle();
					listener.muteToggled(trackNum,
							((BeatBotToggleButton) releasedButton).isOn());
				} else if (releasedButton.equals(soloButton)) {
					BeatBotToggleButton soloButton = ((BeatBotToggleButton) releasedButton);
					soloButton.toggle();
					listener.soloToggled(trackNum, soloButton.isOn());
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

		private BeatBotButton getButton(float x) {
			if (x < buttonRows.get(0).instrumentButton.getIconWidth()) {
				return instrumentButton;
			} else if (x < buttonRows.get(0).instrumentButton.getIconWidth()
					+ buttonRows.get(0).muteButton.getIconWidth()) {
				return muteButton;
			} else if (x < width) {
				return soloButton;
			} else {
				return null;
			}
		}

		private BeatBotButton getSelectedButton() {
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
	public static float width;
	public static float height, trackHeight;

	private static MidiTrackControlListener listener;
	private static FloatBuffer bgRectVb = null;
	private static List<ButtonRow> buttonRows = new ArrayList<ButtonRow>();
	private static Map<Integer, ButtonRow> whichRowOwnsPointer = new HashMap<Integer, ButtonRow>();

	public static void init() {
		if (!buttonRows.isEmpty()) {
			// it is possible that this static class can be reinstantiated after switching between views.
			// ensure we're not re-adding rows by exiting
			return;
		}
		for (int i = 0; i < GlobalVars.tracks.size(); i++) {
			buttonRows.add(new ButtonRow(i,
					GlobalVars.tracks.get(i).getInstrument().getBBIconSource()));
		}
		width = buttonRows.get(0).width;
		MidiViewBean.X_OFFSET = width;
		trackHeight = buttonRows.get(0).instrumentButton.getIconHeight();
		height = trackHeight * buttonRows.size();
		initBgRectVb();
	}

	public static void updateInstrumentIcon(int trackNum) {
		if (trackNum < 0 || trackNum >= buttonRows.size()) {
			return;
		}
		buttonRows.get(trackNum).setIconSource(GlobalVars.tracks.get(trackNum).getInstrument().getBBIconSource());
	}
	
	public static void addListener(MidiTrackControlListener listener) {
		MidiTrackControlHelper.listener = listener;
	}
	
	public static void addTrack(int trackNum, BeatBotIconSource instrumentIcon) {
		buttonRows.add(new ButtonRow(trackNum, instrumentIcon));
		height = trackHeight * buttonRows.size();
		initBgRectVb();
	}
	
	/** draw background color & track control icons */
	public static void draw() {
		SurfaceViewBase.drawTriangleStrip(bgRectVb, Colors.BG_COLOR);
		float y = GlobalVars.midiView.getBean().getHeight() - trackHeight
				- MidiViewBean.Y_OFFSET;
		for (ButtonRow buttonRow : buttonRows) {
			buttonRow.draw(y);
			y -= trackHeight;
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

	public static void handleMove(MotionEvent e) {
		for (int i = 0; i < e.getPointerCount(); i++) {
			int id = e.getPointerId(i);
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
		bgRectVb = SurfaceViewBase.makeRectFloatBuffer(0, 0, width, height
				+ MidiViewBean.Y_OFFSET);
	}
}
