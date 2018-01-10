package com.odang.beatbot.listener;

import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.midi.MidiNote;

public interface MidiNoteListener {
    void onCreate(MidiNote note);

    void onDestroy(MidiNote note);

    void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
                int endNoteValue, long endOnTick, long endOffTick);

    void onSelectStateChange(MidiNote note);

    void beforeLevelChange(MidiNote note);

    void onLevelChange(MidiNote note, LevelType type);
}
