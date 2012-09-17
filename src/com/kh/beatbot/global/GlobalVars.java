package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Typeface;

import com.kh.beatbot.R;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.view.MidiView;

public class GlobalVars {
	public static Typeface font;

	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;
	// time (in millis) for a long press in one location
	public final static long LONG_CLICK_TIME = 500;

	public static final ArrayList<String> currentInstruments = new ArrayList<String>(Arrays.asList(new String[] { "kick", "snare",
			"hh_closed", "hh_open", "rim" }));

	public static final Map<String, Integer> instrumentIcons = new HashMap<String, Integer>();
	static {
		instrumentIcons.put("kick", R.drawable.kick_icon_src);
		instrumentIcons.put("snare", R.drawable.snare_icon_src);
		instrumentIcons.put("hh_closed", R.drawable.hh_closed_icon_src);
		instrumentIcons.put("hh_open", R.drawable.hh_open_icon_src);
		instrumentIcons.put("rim", R.drawable.rimshot_icon_src);
		instrumentIcons.put("recorded", R.drawable.microphone_icon_src);
	}

	public static final float[] BG_COLOR = { 0.12549f, 0.188235f, 0.227451f, 1 };
	public static final float[] WHITE = { 1, 1, 1, 1 };
	public static final float[] GREEN = { 0, 1, 0, 1 };
	public static final float[] YELLOW = { 1, 1, 0, 1 };
	public static final float[] RED = { 1, 0, 0, 1 };
	public static final int UNDO_STACK_SIZE = 40;
	public static final int NUM_EFFECTS = 7;
	public static final short LEVEL_MAX = 127;

	public static MidiView midiView;
	public static String appDirectory;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	public static List<Track> tracks = new ArrayList<Track>();
	public static float currBeatDivision;

	public static void init() {
		for (String instrumentName : currentInstruments) {
			tracks.add(new Track(instrumentName, instrumentIcons.get(instrumentName)));
		}
	}
}
