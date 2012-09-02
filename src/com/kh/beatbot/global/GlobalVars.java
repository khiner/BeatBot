package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.EffectActivity.EffectParam;

public class GlobalVars {
	// time (in millis) between pointer down and pointer up to be considered a tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;
	
	public static final int CHORUS_EFFECT_NUM = 0;
	public static final int DECIMATE_EFFECT_NUM = 1;
	public static final int DELAY_EFFECT_NUM = 2;
	public static final int FILTER_EFFECT_NUM = 3;
	public static final int FLANGER_EFFECT_NUM = 4;
	public static final int REVERB_EFFECT_NUM = 5;
	public static final int TREMELO_EFFECT_NUM = 6;
	
	public static final float[] BG_COLOR = { 0.12549f, 0.188235f, 0.227451f, 1 };
	public static final float[] WHITE = { 1, 1, 1, 1 };
	public static final float[] GREEN = { 0, 1, 0, 1 };
	public static final float[] YELLOW = { 1, 1, 0, 1 };
	public static final float[] RED = { 1, 0, 0, 1 };
	public static final int UNDO_STACK_SIZE = 40;
	public static final int NUM_EFFECTS = 7;
	public static final short LEVEL_MAX = 127;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	public static boolean[] delayParamsLinked;
	public static boolean[] tremeloParamsLinked;
	public static boolean[][] effectOn;
	public static List<EffectParam>[][] params;

	public static int     numTracks;
	public static int[]   filterMode;
	public static float[] trackVolume;
	public static float[] trackPan;
	public static float[] trackPitch;
	public static float[][][] adsrPoints;
	public static float[] sampleLoopBegin;
	public static float[] sampleLoopEnd;
	public static float currBeatDivision;

	private static void initDefaultAdsrPoints(int trackNum) {
		for (int i = 0; i < 5; i++) {
			// x coords
			adsrPoints[trackNum][i][0] = i / 4f;
		}
		// y coords
		adsrPoints[trackNum][0][1] = 0;
		adsrPoints[trackNum][1][1] = 1;
		adsrPoints[trackNum][2][1] = .60f;
		adsrPoints[trackNum][3][1] = .60f;
		adsrPoints[trackNum][4][1] = 0;
	}
	
	public static void init(int numTracks) {
		GlobalVars.numTracks = numTracks;
		params = (ArrayList<EffectParam>[][]) new ArrayList[numTracks][NUM_EFFECTS];
		effectOn = new boolean[numTracks][NUM_EFFECTS];
		filterMode = new int[numTracks];
		delayParamsLinked = new boolean[numTracks];
		tremeloParamsLinked = new boolean[numTracks];
		trackVolume = new float[numTracks];
		trackPan = new float[numTracks];
		trackPitch = new float[numTracks];
		adsrPoints = new float[numTracks][5][2];
		sampleLoopBegin = new float[numTracks];
		sampleLoopEnd = new float[numTracks];
		for (int track = 0; track < numTracks; track++) {
			initDefaultAdsrPoints(track);
			sampleLoopBegin[track] = sampleLoopEnd[track] = 0;
			filterMode[track] = 0;
			delayParamsLinked[track] = tremeloParamsLinked[track] = true;
			trackVolume[track] = .8f;
			trackPan[track] = trackPitch[track] = .5f;
			for (int effect = 0; effect < NUM_EFFECTS; effect++) {
				effectOn[track][effect] = false;
				params[track][effect] = new ArrayList<EffectParam>();
			}
		}
	}
}
