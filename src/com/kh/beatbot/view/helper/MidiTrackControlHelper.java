package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BeatBotButton;
import com.kh.beatbot.global.BeatBotToggleButton;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.IconIds;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;

public class MidiTrackControlHelper {
	public static class ButtonRow {
		int trackNum;
		float width;
		BeatBotButton instrumentButton;
		BeatBotToggleButton muteButton, soloButton;

		public ButtonRow(IconIds iconIds) {
			instrumentButton = new BeatBotButton(iconIds.defaultId,
					iconIds.selectedId);
			muteButton = new BeatBotToggleButton(R.drawable.mute_icon,
					R.drawable.mute_icon_selected);
			soloButton = new BeatBotToggleButton(R.drawable.solo_icon,
					R.drawable.solo_icon_selected);
			width = instrumentButton.getIconWidth() + muteButton.getIconWidth()
					+ soloButton.getIconWidth();
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

		public void handleLongPress(float x) {
			BeatBotButton pressedButton = getButton(x);
			if (pressedButton.equals(instrumentButton) && pressedButton.isTouched()) {
				listener.trackLongPressed(trackNum);
			}
		}
		
		public void handleRelease(float x) {
			BeatBotButton releasedButton = getButton(x);
			if (releasedButton != null) {
				if (releasedButton.equals(muteButton)) {
					((BeatBotToggleButton) releasedButton).toggle();
					listener.muteToggled(trackNum, ((BeatBotToggleButton) releasedButton).isOn());
				} else if (releasedButton.equals(soloButton)) {
					((BeatBotToggleButton) releasedButton).toggle();
					listener.soloToggled(trackNum, ((BeatBotToggleButton) releasedButton).isOn());
				}
			}
			instrumentButton.release();
			muteButton.release();
			soloButton.release();
		}

		private BeatBotButton getButton(float x) {
			if (x < buttonRows.get(0).instrumentButton.getIconWidth()) {
				return instrumentButton;
			} else if (x < buttonRows.get(0).muteButton.getIconWidth()) {
				return muteButton;
			} else if (x < width) {
				return soloButton;
			} else {
				return null;
			}
		}
	}

	public static final int NUM_CONTROLS = 3; // mute/solo/track settings
	public static float width;
	public static float height;

	private static MidiTrackControlListener listener;
	private static FloatBuffer bgRectVb = null;
	private static List<ButtonRow> buttonRows = new ArrayList<ButtonRow>();

	public static void init() {
		for (int i = 0; i < GlobalVars.tracks.size(); i++) {
			buttonRows.add(new ButtonRow(
					GlobalVars.tracks.get(i).instrumentIcon));
		}
		width = buttonRows.get(0).width;
		height = GlobalVars.midiView.getBean().getHeight();
		MidiViewBean.X_OFFSET = width;
		MidiViewBean.setMinTrackHeight(buttonRows.get(0).instrumentButton
				.getIconHeight());
		initBgRectVb();
	}

	public static void addListener(MidiTrackControlListener listener) {
		MidiTrackControlHelper.listener = listener;
	}

	/** draw background color & track control icons */
	public static void draw() {
		SurfaceViewBase.drawTriangleStrip(bgRectVb, Colors.BG_COLOR);
		float y = height - MidiViewBean.minTrackHeight;
		for (ButtonRow buttonRow : buttonRows) {
			buttonRow.draw(y);
			y -= MidiViewBean.minTrackHeight;
		}
	}

	public static void handlePress(float x, int track) {
		if (x > width || listener == null || track < 0
				|| track >= buttonRows.size())
			return;
		ButtonRow selectedRow = buttonRows.get(track);
		selectedRow.handlePress(x);
	}

	public static void handleLongPress(float x, int track) {
		if (x > width || listener == null || track < 0
				|| track >= buttonRows.size())
			return;
		ButtonRow selectedRow = buttonRows.get(track);
		selectedRow.handleLongPress(x);
	}

	public static void handleRelease(float x, int track) {
		if (x > width || listener == null || track < 0
				|| track >= buttonRows.size())
			return;
		ButtonRow selectedRow = buttonRows.get(track);
		selectedRow.handleRelease(x);
	}

	private static void initBgRectVb() {
		bgRectVb = SurfaceViewBase.makeRectFloatBuffer(0, 0, width, height);
	}
}
