package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Typeface;
import android.util.Log;

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

	public static final Map<String, Integer> instrumentSources = new HashMap<String, Integer>();
	
	public static Map<String, BeatBotIconSource> instrumentIcons = new HashMap<String, BeatBotIconSource>();
	static {
		instrumentSources.put("kick", R.drawable.kick_icon_src);
		instrumentSources.put("snare", R.drawable.snare_icon_src);
		instrumentSources.put("hh_closed", R.drawable.hh_closed_icon_src);
		instrumentSources.put("hh_open", R.drawable.hh_open_icon_src);
		instrumentSources.put("rim", R.drawable.rimshot_icon_src);
		instrumentSources.put("recorded", R.drawable.microphone_icon_src);
		instrumentIcons.put("kick", new BeatBotIconSource());
		instrumentIcons.put("snare", new BeatBotIconSource());
		instrumentIcons.put("hh_closed", new BeatBotIconSource());
		instrumentIcons.put("hh_open", new BeatBotIconSource());
		instrumentIcons.put("rim", new BeatBotIconSource());
		instrumentIcons.put("recorded", new BeatBotIconSource());
	}
	
	public static BeatBotIconSource muteIcon, soloIcon, previewIcon, beatSyncIcon;
	
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

	public static void initTracks() {
		for (String instrumentName : currentInstruments) {
			tracks.add(new Track(instrumentName, instrumentIcons
					.get(instrumentName)));
		}
	}

	public static void initIcons() {
		muteIcon = new BeatBotIconSource(R.drawable.mute_icon,
				R.drawable.mute_icon_selected);
		soloIcon = new BeatBotIconSource(R.drawable.solo_icon,
				R.drawable.solo_icon_selected);
		previewIcon = new BeatBotIconSource(R.drawable.preview_icon, R.drawable.preview_icon_selected);
		beatSyncIcon = new BeatBotIconSource(R.drawable.clock, R.drawable.note_icon);
		instrumentIcons.get("kick").set(R.drawable.kick_icon_small,
				R.drawable.kick_icon_selected_small);
		instrumentIcons.get("snare").set(R.drawable.snare_icon_small,
						R.drawable.snare_icon_selected_small);
		instrumentIcons.get("hh_closed").set(R.drawable.hh_closed_icon_small,
				R.drawable.hh_closed_icon_selected_small);
		instrumentIcons.get("hh_open").set(R.drawable.hh_open_icon_small,
				R.drawable.hh_open_icon_selected_small);
		instrumentIcons.get("rim").set(R.drawable.rimshot_icon_small,
				R.drawable.rimshot_icon_selected_small);
		instrumentIcons.get("recorded").set(R.drawable.microphone_icon_small,
				R.drawable.microphone_icon_selected_small);
	}
}
