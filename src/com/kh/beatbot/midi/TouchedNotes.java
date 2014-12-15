package com.kh.beatbot.midi;

import android.util.SparseArray;

public class TouchedNotes extends SparseArray<MidiNote> {
	public boolean isEmpty() {
		return size() <= 0;
	}
}
