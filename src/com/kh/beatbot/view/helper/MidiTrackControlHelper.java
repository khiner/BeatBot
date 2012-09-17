package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;

public class MidiTrackControlHelper {
	private static final int MUTE_ICON_ID = 0;
	private static final int SOLO_ICON_ID = 1;
	
	public static final int NUM_CONTROLS = 3; // mute/solo/track settings
	public static float width;
	public static float height;
	
	private static MidiTrackControlListener listener;
	private static FloatBuffer bgRectVb = null;
	
	public static void init() {
		GlobalVars.midiView.loadTexture(R.drawable.mute_icon, MUTE_ICON_ID);
		GlobalVars.midiView.loadTexture(R.drawable.solo_icon, SOLO_ICON_ID);
		for (int i = 0; i < GlobalVars.tracks.size(); i++) {
			//GlobalVars.midiView.loadTexture(GlobalVars.tracks.get(i).instrumentIcon, i + 2);
			GlobalVars.midiView.loadTexture(R.drawable.kick_icon_small, i + 2);
		}
		width = 3 * GlobalVars.midiView.getTextureWidth(0);
		height = GlobalVars.midiView.getBean().getHeight();
		MidiViewBean.X_OFFSET = width;
		MidiViewBean.setMinTrackHeight(GlobalVars.midiView.getTextureHeight(0));
		initBgRectVb();
	}
	
	public static void addListener(MidiTrackControlListener listener) {
		MidiTrackControlHelper.listener = listener;
	}
	
	/** draw background color & track control icons */
	public static void draw() {
		SurfaceViewBase.drawTriangleStrip(bgRectVb, Colors.BG_COLOR);
		float y = 0;
		for (int i = 0; i < GlobalVars.tracks.size(); i++) {
			GlobalVars.midiView.drawTexture(i + 2, 0, y);
			GlobalVars.midiView.drawTexture(MUTE_ICON_ID, width / 3, y);
			GlobalVars.midiView.drawTexture(SOLO_ICON_ID, 2 * width / 3, y);
			y += MidiViewBean.minTrackHeight;
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
