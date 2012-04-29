package com.kh.beatbot;

import android.app.Application;

public class GlobalVars extends Application {
	MidiManager midiManager = null;

	public MidiManager getMidiManager() {
		return midiManager;
	}

	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
	}	
}
