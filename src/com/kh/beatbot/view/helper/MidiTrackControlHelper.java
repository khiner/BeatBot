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
		float width;
		BeatBotButton instrumentButton;
		BeatBotToggleButton muteButton, soloButton;
		
		public ButtonRow(IconIds iconIds) {
			instrumentButton = new BeatBotButton(iconIds.defaultId, iconIds.selectedId);
			muteButton = new BeatBotToggleButton(R.drawable.mute_icon, R.drawable.mute_icon_selected);
			soloButton = new BeatBotToggleButton(R.drawable.solo_icon, R.drawable.solo_icon_selected);
			width = instrumentButton.getIconWidth() + muteButton.getIconWidth() + soloButton.getIconWidth();
		}
		
		public void draw(float y) {
			instrumentButton.draw(0, y);
			muteButton.draw(instrumentButton.getIconWidth(), y);
			soloButton.draw(instrumentButton.getIconWidth() + muteButton.getIconWidth(), y);
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
			buttonRows.add(new ButtonRow(GlobalVars.tracks.get(i).instrumentIcon));
		}
		width = buttonRows.get(0).width;
		height = GlobalVars.midiView.getBean().getHeight();
		MidiViewBean.X_OFFSET = width;
		MidiViewBean.setMinTrackHeight(buttonRows.get(0).instrumentButton.getIconHeight());
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
	
	public static void handleClick(float x, int track) {
		if (x > width || listener == null)
			return;
		listener.midiControlIconClicked(track, xToControlNum(x));
	}
	
	public static void handleLongPress(float x, int track) {
		if (x > width || listener == null)
			return;
		listener.midiControlIconLongPressed(track, xToControlNum(x));
	}

	private static void initBgRectVb() {
		bgRectVb = SurfaceViewBase.makeRectFloatBuffer(0, 0, width, height);
	}
	
	private static int xToControlNum(float x) {
		return (int)((x / width) * NUM_CONTROLS);
	}
}
