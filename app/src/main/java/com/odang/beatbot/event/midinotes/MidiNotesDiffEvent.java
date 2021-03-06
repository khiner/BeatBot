package com.odang.beatbot.event.midinotes;

import com.odang.beatbot.event.Stateful;
import com.odang.beatbot.ui.view.View;

import java.util.ArrayList;
import java.util.List;

public class MidiNotesDiffEvent implements Stateful {
    private final List<MidiNoteDiff> midiNoteDiffs;

    public MidiNotesDiffEvent(MidiNoteDiff midiNoteDiff) {
        midiNoteDiffs = new ArrayList<>(1);
        midiNoteDiffs.add(midiNoteDiff);
    }

    public MidiNotesDiffEvent(final List<MidiNoteDiff> midiNoteDiffs) {
        this.midiNoteDiffs = midiNoteDiffs;
    }

    @Override
    public void undo() {
        final List<MidiNoteDiff> oppositeDiffs = new ArrayList<>(midiNoteDiffs.size());
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
