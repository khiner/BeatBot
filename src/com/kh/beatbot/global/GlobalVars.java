package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Typeface;

import com.kh.beatbot.R;
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

	public static final ArrayList<String> currentInstruments = new ArrayList<String>(
			Arrays.asList(new String[] { "kick", "snare", "hh_closed",
					"hh_open", "rim" }));

	public static final Map<String, IconIds> instrumentIcons = new HashMap<String, IconIds>();
	static {
		instrumentIcons.put("kick", new IconIds(R.drawable.kick_icon_small,
				R.drawable.kick_icon_selected_small, R.drawable.kick_icon_src));
		instrumentIcons.put("snare",
				new IconIds(R.drawable.snare_icon_small,
						R.drawable.snare_icon_selected_small,
						R.drawable.snare_icon_src));
		instrumentIcons.put("hh_closed", new IconIds(
				R.drawable.hh_closed_icon_small,
				R.drawable.hh_closed_icon_selected_small,
				R.drawable.hh_closed_icon_src));
		instrumentIcons.put("hh_open", new IconIds(
				R.drawable.hh_open_icon_small,
				R.drawable.hh_open_icon_selected_small,
				R.drawable.hh_open_icon_src));
		instrumentIcons.put("rim", new IconIds(R.drawable.rimshot_icon_small,
				R.drawable.rimshot_icon_selected_small,
				R.drawable.rimshot_icon_src));
		instrumentIcons.put("recorded", new IconIds(
				R.drawable.microphone_icon_small,
				R.drawable.microphone_icon_selected_small,
				R.drawable.microphone_icon_src));
	}

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
			tracks.add(new Track(instrumentName, instrumentIcons
					.get(instrumentName)));
		}
	}
}
