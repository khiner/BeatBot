package com.odang.beatbot.event.midinotes;

import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.ui.view.View;

public class MidiNoteDestroyDiff extends MidiNoteDiff {
    MidiNote note;

    public MidiNoteDestroyDiff(MidiNote note) {
        this.note = note;
    }

    @Override
    public void apply() {
        // when restoring from saved file, saved ticks can be different & no Rectangle instance
        final MidiNote noteToDestroy = View.context.getMidiManager().findNote(note.getNoteValue(),
                note.getOnTick());
        if (noteToDestroy != null)
            noteToDestroy.destroy();
    }

    @Override
    public MidiNoteCreateDiff opposite() {
        return new MidiNoteCreateDiff(note);
    }
}
