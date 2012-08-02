package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.EffectActivity.EffectParam;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;

public class GlobalVars {
	public static final int UNDO_STACK_SIZE = 40;
	public static final short LEVEL_MAX = 127;
	private static MidiManager midiManager = null;
	private static PlaybackManager playbackManager = null;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	public static List<EffectParam>[] params;
	
	public static boolean[] chorusOn, decimateOn, delayOn, delayBeatmatch,
			flangerOn, filterOn, reverbOn, tremeloOn;

	public static float currBeatDivision;

	public static MidiManager getMidiManager() {
		return midiManager;
	}

	public static void setMidiManager(MidiManager midiManager) {
		GlobalVars.midiManager = midiManager;
		int num = midiManager.getNumSamples();
		params = (ArrayList<EffectParam>[])new ArrayList[num];
		for (int i = 0; i < num; i++)
			params[i] = new ArrayList<EffectParam>();
		chorusOn = new boolean[num];
		decimateOn = new boolean[num];
		delayOn = new boolean[num];
		delayBeatmatch = new boolean[num];
		flangerOn = new boolean[num];
		filterOn = new boolean[num];
		reverbOn = new boolean[num];
		tremeloOn = new boolean[num];
		for (int i = 0; i < midiManager.getNumSamples(); i++) {
			chorusOn[i] = decimateOn[i] = delayOn[i] = delayBeatmatch[i] = filterOn[i] = flangerOn[i] = reverbOn[i] = tremeloOn[i] = false;
		}
	}

	public static PlaybackManager getPlaybackManager() {
		return playbackManager;
	}

	public static void setPlaybackManager(PlaybackManager playbackManager) {
		GlobalVars.playbackManager = playbackManager;
	}
}
