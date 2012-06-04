package com.kh.beatbot.global;

import android.app.Application;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;

public class GlobalVars extends Application {
	public static final int LEVEL_MAX = 127;
	
	private MidiManager midiManager = null;
	private PlaybackManager playbackManager = null;
	
	public MidiManager getMidiManager() {
		return midiManager;
	}
	
	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
	}	
	
	public PlaybackManager getPlaybackManager() {
		return playbackManager;
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}
}
