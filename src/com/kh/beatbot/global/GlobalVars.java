package com.kh.beatbot.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
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

	public static final String[] instrumentNames = { "kick", "snare", "hh_closed",
			"hh_open", "rim", "recorded" };
	public static final String[] defaultInstruments = { "kick", "snare", "hh_closed",
			"hh_open", "rim" };

	public final static int[] instrumentIcons = { R.drawable.kick_icon_src,
			R.drawable.snare_icon_src, R.drawable.hh_closed_icon_src,
			R.drawable.hh_open_icon_src, R.drawable.rimshot_icon_src,
			R.drawable.microphone_icon_src };

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
	public static List<Effect>[] effects;

	public static int numTracks;
	public static float[] trackVolume;
	public static float[] trackPan;
	public static float[] trackPitch;
	public static float[][][] adsrPoints;
	public static float[] sampleLoopBegin;
	public static float[] sampleLoopEnd;
	public static float currBeatDivision;
	public static File[][] samples;
	public static String[][] sampleNames;

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
		effects = (ArrayList<Effect>[]) new ArrayList[numTracks];
		trackVolume = new float[numTracks];
		trackPan = new float[numTracks];
		trackPitch = new float[numTracks];
		adsrPoints = new float[numTracks][5][2];
		sampleLoopBegin = new float[numTracks];
		sampleLoopEnd = new float[numTracks];
		samples = new File[numTracks + 1][];
		sampleNames = new String[numTracks + 1][];
		for (int track = 0; track < numTracks + 1; track++) {
			File dir = new File(appDirectory + instrumentNames[track]);
			samples[track] = dir.listFiles();
			sampleNames[track] = dir.list();
			if (track >= numTracks)
				continue;
			initDefaultAdsrPoints(track);
			sampleLoopBegin[track] = sampleLoopEnd[track] = 0;
			trackVolume[track] = .8f;
			trackPan[track] = trackPitch[track] = .5f;
			effects[track] = new ArrayList<Effect>();
		}
	}

	public static byte[] getSampleBytes(int trackNum, int sampleNum) {
		byte[] bytes = null;
		try {
			File sampleFile = samples[trackNum][sampleNum];
			FileInputStream in = new FileInputStream(sampleFile);
			bytes = new byte[(int) sampleFile.length()];
			in.read(bytes);
			in.close();
			in = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public static Effect findEffectById(int effectId, int trackNum) {
		for (Effect effect : GlobalVars.effects[trackNum]) {
			if (effect.getId() == effectId) {
				return effect;
			}
		}
		return null;
	}

	public static Effect findEffectByPosition(int position, int trackNum) {
		for (Effect effect : GlobalVars.effects[trackNum]) {
			if (effect.getPosition() == position) {
				return effect;
			}
		}
		return null;
	}
}
