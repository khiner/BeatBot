package com.kh.beatbot.global;

import android.graphics.Typeface;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.view.BpmView;
import com.kh.beatbot.view.MidiView;

public class GlobalVars {
	public static BeatBotActivity mainActivity;

	public static Typeface font;
	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;
	// time (in millis) for a long press in one location
	public final static long LONG_CLICK_TIME = 500;

	public final static int MAX_EFFECTS_PER_TRACK = 3; // also in jni/Track.h,
														// ugly but necessary

	public static float MASTER_VOL_LEVEL = .8f;
	public static float MASTER_PAN_LEVEL = .5f;
	public static float MASTER_PIT_LEVEL = .5f;

	public static final int UNDO_STACK_SIZE = 40;
	public static final int NUM_EFFECTS = 7;
	public static final short LEVEL_MAX = 127;

	public static enum LevelType {
		VOLUME, PAN, PITCH
	};

	public static MidiView midiView;
	public static BpmView bpmView;

	public static float currBeatDivision;
}
