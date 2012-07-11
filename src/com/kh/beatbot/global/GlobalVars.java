package com.kh.beatbot.global;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;

public class GlobalVars {
	public static final int LEVEL_MAX = 127;
	
	private static MidiManager midiManager = null;
	private static PlaybackManager playbackManager = null;
	
	// effect settings are stored here instead of in the effect activities because
	// the activities are destroyed after clicking 'back', and we need to persist state
	public static float[] decimateX, decimateY, delayX, delayY, delayWet,
						  filterX, filterY, reverbX, reverbY;
	
	public static boolean[] decimateOn, delayOn, filterOn, reverbOn;
	
	public static float currBeatDivision;
	
	public static MidiManager getMidiManager() {
		return midiManager;
	}
	
	public static void setMidiManager(MidiManager midiManager) {
		GlobalVars.midiManager = midiManager;
		int num = midiManager.getNumSamples();
		decimateX = new float[num];
		decimateY = new float[num];
		delayX = new float[num];
		delayY = new float[num];
		delayWet = new float[num];
		filterX = new float[num];
		filterY = new float[num];
		reverbX = new float[num];
		reverbY = new float[num];
		decimateOn = new boolean[num];
		delayOn = new boolean[num];
		filterOn = new boolean[num];
		reverbOn = new boolean[num];
		for (int i = 0; i < midiManager.getNumSamples(); i++) { 
			decimateX[i] = decimateY[i] = delayX[i] = delayY[i] =
						   filterX[i] = filterY[i] = reverbY[i] = reverbX[i] = .5f;
			decimateOn[i] = delayOn[i] = filterOn[i] = reverbOn[i] = false;
		}
	}	
	
	public static PlaybackManager getPlaybackManager() {
		return playbackManager;
	}

	public static void setPlaybackManager(PlaybackManager playbackManager) {
		GlobalVars.playbackManager = playbackManager;
	}
}
