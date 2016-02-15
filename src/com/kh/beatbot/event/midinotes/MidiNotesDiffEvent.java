package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.ui.view.View;

public class MidiNotesDiffEvent implements Stateful {
	private final List<MidiNoteDiff> midiNoteDiffs;

	public MidiNotesDiffEvent(MidiNoteDiff midiNoteDiff) {
		midiNoteDiffs = new ArrayList<MidiNoteDiff>(1);
		midiNoteDiffs.add(midiNoteDiff);
	}

	public MidiNotesDiffEvent(final List<MidiNoteDiff> midiNoteDiffs) {
		this.midiNoteDiffs = midiNoteDiffs;
	}

	public List<MidiNoteDiff> getMidiNoteDiffs() {
		return midiNoteDiffs;
	}

	@Override
	public void undo() {
		final List<MidiNoteDiff> oppositeDiffs = new ArrayList<MidiNoteDiff>(midiNoteDiffs.size());
		// apply opposites in reverse order
		for (int i = midiNoteDiffs.size() - 1; i >= 0; i--) {
			oppositeDiffs.add(midiNoteDiffs.get(i).opposite());
		}
		View.context.getMidiManager().applyDiffs(oppositeDiffs);
	}

	@Override
	public void apply() {
		View.context.getMidiManager().applyDiffs(midiNoteDiffs);
	}
}
