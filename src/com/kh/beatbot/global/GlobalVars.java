package com.kh.beatbot.global;

import android.graphics.Typeface;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.view.group.EffectPage;
import com.kh.beatbot.view.group.MainPage;

public class GlobalVars {

	public static Typeface font;

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

	public static BeatBotActivity mainActivity;
	public static MainPage mainPage;
	public static EffectPage effectPage;
	
	public static float currBeatDivision;
}
