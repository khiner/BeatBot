package com.kh.beatbot.manager;

import android.os.Bundle;

public class Managers {
	public static MidiManager midiManager = null;
	public static PlaybackManager playbackManager = null;
	public static RecordManager recordManager = null;

	public static void init(Bundle savedInstanceState, String[] sampleNames) {
		// get all Manager singletons
		playbackManager = PlaybackManager.getInstance(sampleNames);
		recordManager = RecordManager.getInstance();
		
		// if this context is being restored from a destroyed context,
		// recover the midiManager. otherwise, create a new one
		if (savedInstanceState == null) {
			midiManager = MidiManager.getInstance(sampleNames.length);
		} else {
			midiManager = savedInstanceState.getParcelable("midiManager");
		}
	}
}
