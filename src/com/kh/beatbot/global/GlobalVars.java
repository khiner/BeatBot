package com.kh.beatbot.global;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;

public class GlobalVars {
	public static final int UNDO_STACK_SIZE = 40;
	public static final short LEVEL_MAX = 127;
	private static MidiManager midiManager = null;
	private static PlaybackManager playbackManager = null;

	// effect settings are stored here instead of in the effect activities
	// because
	// the activities are destroyed after clicking 'back', and we need to
	// persist state
	public static float[] chorusX, chorusY, chorusWet, chorusModRate,
			chorusModAmt, decimateX, decimateY, delayX, delayY, delayWet,
			flangerX, flangerY, flangerWet, flangerModRate, flangerModAmt,
			flangerPhase, filterX, filterY, reverbX, reverbY;

	public static boolean[] chorusOn, decimateOn, delayOn, flangerOn, filterOn,
			reverbOn;

	public static float currBeatDivision;

	public static MidiManager getMidiManager() {
		return midiManager;
	}

	public static void setMidiManager(MidiManager midiManager) {
		GlobalVars.midiManager = midiManager;
		int num = midiManager.getNumSamples();
		chorusX = new float[num];
		chorusY = new float[num];
		chorusWet = new float[num];
		chorusModRate = new float[num];
		chorusModAmt = new float[num];
		decimateX = new float[num];
		decimateY = new float[num];
		delayX = new float[num];
		delayY = new float[num];
		delayWet = new float[num];
		flangerX = new float[num];
		flangerY = new float[num];
		flangerWet = new float[num];
		flangerModRate = new float[num];
		flangerModAmt = new float[num];
		flangerPhase = new float[num];
		filterX = new float[num];
		filterY = new float[num];
		reverbX = new float[num];
		reverbY = new float[num];
		chorusOn = new boolean[num];
		decimateOn = new boolean[num];
		delayOn = new boolean[num];
		flangerOn = new boolean[num];
		filterOn = new boolean[num];
		reverbOn = new boolean[num];
		for (int i = 0; i < midiManager.getNumSamples(); i++) {
			chorusX[i] = chorusY[i] = chorusWet[i] = chorusModRate[i] = chorusModAmt[i] = flangerX[i] = flangerY[i] = flangerWet[i] = flangerModRate[i] = flangerModAmt[i] = decimateX[i] = decimateY[i] = delayX[i] = delayY[i] = filterX[i] = filterY[i] = reverbY[i] = reverbX[i] = .5f;
			flangerPhase[i] = 1;
			chorusOn[i] = decimateOn[i] = delayOn[i] = filterOn[i] = flangerOn[i] = reverbOn[i] = false;
		}
	}

	public static PlaybackManager getPlaybackManager() {
		return playbackManager;
	}

	public static void setPlaybackManager(PlaybackManager playbackManager) {
		GlobalVars.playbackManager = playbackManager;
	}
}
