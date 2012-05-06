package com.kh.beatbot.global;

import com.kh.beatbot.manager.MidiManager;

import android.app.Application;

public class GlobalVars extends Application {
	public static final int LEVEL_MAX = 127;
	
	private MidiManager midiManager = null;
		
	public MidiManager getMidiManager() {
		return midiManager;
	}
	
	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
	}	
}
