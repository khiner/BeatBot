package com.odang.beatbot.manager;

import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.event.midinotes.MidiNoteDiff;
import com.odang.beatbot.event.midinotes.MidiNotesEventManager;
import com.odang.beatbot.event.track.TrackCreateEvent;
import com.odang.beatbot.file.MidiFile;
import com.odang.beatbot.listener.LoopWindowListener;
import com.odang.beatbot.listener.MidiNoteListener;
import com.odang.beatbot.listener.SnapToGridListener;
import com.odang.beatbot.listener.TempoListener;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.midi.MidiTrack;
import com.odang.beatbot.midi.event.MidiEvent;
import com.odang.beatbot.midi.event.NoteOff;
import com.odang.beatbot.midi.event.NoteOn;
import com.odang.beatbot.midi.event.meta.Tempo;
import com.odang.beatbot.midi.event.meta.TimeSignature;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.track.Track;

import java.util.ArrayList;
import java.util.List;

import static com.odang.beatbot.ui.view.View.context;

public class MidiManager implements MidiNoteListener {
    public static final int MIN_BPM = 45, MAX_BPM = 300,
            TICKS_PER_NOTE = MidiFile.DEFAULT_RESOLUTION,
            NOTES_PER_MEASURE = 4, TICKS_PER_MEASURE = TICKS_PER_NOTE * NOTES_PER_MEASURE,
            MIN_TICKS = TICKS_PER_NOTE / 2, MAX_TICKS = TICKS_PER_MEASURE * 4;

    private static final double LOG_2 = Math.log(2);

    private boolean snapToGrid = true;
    private int beatDivision = 2;
    private long loopBeginTick, loopEndTick;

    private TimeSignature ts = new TimeSignature();
    private Tempo tempo = new Tempo();
    private MidiTrack tempoTrack = new MidiTrack();
    private List<MidiNote> copiedNotes = new ArrayList<>();

    private List<MidiNoteListener> midiNoteListeners = new ArrayList<>();
    private List<LoopWindowListener> loopChangeListeners = new ArrayList<>();
    private List<TempoListener> tempoListeners = new ArrayList<>();
    private SnapToGridListener snapToGridListener;

    private MidiNotesEventManager midiNotesEventManager;

    public MidiManager() {
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(tempo);
        setBpm(getBpm()); // set native bpm & trigger event notification

        midiNotesEventManager = new MidiNotesEventManager(this);
    }

    public void addMidiNoteListener(MidiNoteListener listener) {
        midiNoteListeners.add(listener);
    }

    public void addLoopChangeListener(LoopWindowListener listener) {
        loopChangeListeners.add(listener);
    }

    public void addTempoListener(TempoListener listener) {
        tempoListeners.add(listener);
    }

    public void setSnapToGridListener(SnapToGridListener listener) {
        snapToGridListener = listener;
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
        snapToGridListener.onSnapToGridChanged(snapToGrid);
    }

    public MidiTrack getTempoTrack() {
        return tempoTrack;
    }

    public float getBpm() {
        return tempo.getBpm();
    }

    public void setBpm(float bpm) {
        bpm = GeneralUtils.clipTo(bpm, MIN_BPM, MAX_BPM);
        tempo.setBpm(bpm);
        setNativeMSPT(tempo.getMpqn() / TICKS_PER_NOTE);
        onTempoChange(bpm);
    }

    public void incrementBpm() {
        setBpm(getBpm() + 1);
    }

    public void decrementBpm() {
        setBpm(getBpm() - 1);
    }

    public MidiNote addNote(long onTick, long offTick, int note) {
        return midiNotesEventManager.createNote(note, onTick, offTick);
    }

    public MidiNote findNote(int noteValue, long onTick) {
        final Track track = context.getTrackManager().getTrackByNoteValue(noteValue);
        return null == track ? null : track.findNoteStarting(onTick);
    }

    public MidiNote findNoteContaining(int noteValue, long tick) {
        final Track track = context.getTrackManager().getTrackByNoteValue(noteValue);
        return null == track ? null : track.findNoteContaining(tick);
    }

    public void deleteNote(MidiNote midiNote) {
        midiNotesEventManager.destroyNote(midiNote);
    }

    public void copy() {
        if (context.getTrackManager().anyNoteSelected()) {
            copiedNotes = context.getTrackManager().copySelectedNotes();
        }
    }

    public boolean isCopying() {
        return !copiedNotes.isEmpty();
    }

    public void cancelCopy() {
        copiedNotes.clear();
    }

    public void paste(long startTick) {
        if (copiedNotes.isEmpty())
            return;
        // Copied notes should still be selected, so leftmostSelectedTick should be accurate
        long tickOffset = startTick - context.getTrackManager().getSelectedNoteTickWindow()[0];
        for (MidiNote copiedNote : copiedNotes) {
            copiedNote.setTicks(copiedNote.getOnTick() + tickOffset, copiedNote.getOffTick()
                    + tickOffset);
        }

        midiNotesEventManager.createNotes(copiedNotes);
        copiedNotes.clear();
    }

    public int getBeatDivision() {
        return beatDivision;
    }

    public void adjustBeatDivision(float numTicksDisplayed) {
        beatDivision = (int) (Math.log(MAX_TICKS / numTicksDisplayed) / LOG_2);
    }

    public long getTicksPerBeat() {
        return TICKS_PER_NOTE / (1 << beatDivision);
    }

    public long getMajorTickNearestTo(long tick) {
        long spacing = getMajorTickSpacing();
        long remainder = tick % spacing;
        return remainder == 0 ? tick : (remainder > spacing / 2 ? getMajorTickAfter(tick)
                : getMajorTickBefore(tick));
    }

    public long getMajorTickBefore(long tick) {
        return tick - tick % getMajorTickSpacing();
    }

    public long getMajorTickAfter(long tick) {
        return getMajorTickBefore(tick) + getMajorTickSpacing();
    }

    public void quantize() {
        midiNotesEventManager.quantize(beatDivision);
    }

    public void importFromFile(final MidiFile midiFile) {
        final List<MidiNote> newNotes = new ArrayList<>();
        final List<MidiTrack> midiTracks = midiFile.getTracks();
        tempoTrack = midiTracks.get(0);
        ts = (TimeSignature) tempoTrack.getEvents().get(0);
        tempo = (Tempo) tempoTrack.getEvents().get(1);
        setNativeMSPT(tempo.getMpqn() / TICKS_PER_NOTE);
        final List<MidiEvent> events = midiTracks.get(1).getEvents();

        // midiEvents are ordered by tick, so on/off events don't
        // necessarily alternate if there are interleaving notes (with different "notes"
        // - pitches) thus, we need to keep track of notes that have an on event, but
        // are waiting for the off event
        final List<NoteOn> unfinishedNotes = new ArrayList<>();
        for (final MidiEvent event : events) {
            if (event instanceof NoteOn)
                unfinishedNotes.add((NoteOn) event);
            else if (event instanceof NoteOff) {
                final NoteOff off = (NoteOff) event;
                for (int j = 0; j < unfinishedNotes.size(); j++) {
                    final NoteOn on = unfinishedNotes.get(j);
                    if (on.getNoteValue() == off.getNoteValue()) {
                        // TODO store all midi notes and then add them
                        // together in one event
                        final MidiNote note = new MidiNote(on, off);
                        newNotes.add(note);
                        unfinishedNotes.remove(j);
                        break;
                    }
                }
            }
        }

        // create any necessary tracks
        for (final MidiNote note : newNotes) {
            while (note.getNoteValue() >= context.getTrackManager().getNumTracks()) {
                new TrackCreateEvent().execute();
            }
        }

        beginEvent();
        midiNotesEventManager.destroyAllNotes();
        midiNotesEventManager.createNotes(newNotes);
        endEvent();
    }

    public void beginEvent() {
        midiNotesEventManager.begin();
    }

    public void endEvent() {
        midiNotesEventManager.end();
    }

    public void moveNote(MidiNote note, int noteDiff, long tickDiff) {
        midiNotesEventManager.moveNote(note, noteDiff, tickDiff);
    }

    public void moveSelectedNotes(int noteDiff, long tickDiff) {
        midiNotesEventManager.moveSelectedNotes(noteDiff, tickDiff);
    }

    public void pinchSelectedNotes(long beginTickDiff, long endTickDiff) {
        midiNotesEventManager.pinchSelectedNotes(beginTickDiff, endTickDiff);
    }

    public void applyDiffs(final List<MidiNoteDiff> diffs) {
        context.getTrackManager().saveNoteTicks();
        context.getTrackManager().deselectAllNotes();

        for (MidiNoteDiff midiNoteDiff : diffs) {
            midiNoteDiff.apply();
        }

        midiNotesEventManager.handleNoteCollisions();
        midiNotesEventManager.finalizeNoteTicks();
        context.getTrackManager().deselectAllNotes();
    }

    public void deleteSelectedNotes() {
        midiNotesEventManager.deleteSelectedNotes();
    }

    public void setLoopTicks(long loopBeginTick, long loopEndTick) {
        boolean changed = false;
        long quantizedBeginTick = getMajorTickNearestTo(loopBeginTick);
        long quantizedEndTick = getMajorTickNearestTo(loopEndTick);
        if (quantizedBeginTick != this.loopBeginTick && quantizedBeginTick < quantizedEndTick) {
            this.loopBeginTick = quantizedBeginTick;
            changed = true;
        }

        if (quantizedEndTick != this.loopEndTick && quantizedEndTick > quantizedBeginTick) {
            this.loopEndTick = quantizedEndTick;
            changed = true;
        }

        if (changed) {
            notifyLoopWindowChanged();
        }
    }

    // set new loop begin point, preserving loop length
    public void translateLoopWindowTo(long tick) {
        long loopLength = getLoopLength();
        long newBeginTick = GeneralUtils.clipTo(tick, 0, MAX_TICKS - loopLength);
        setLoopTicks(newBeginTick, newBeginTick + loopLength);
    }

    public long getLoopBeginTick() {
        return loopBeginTick;
    }

    public long getLoopEndTick() {
        return loopEndTick;
    }

    public long getLoopLength() {
        return getLoopEndTick() - getLoopBeginTick();
    }

    public long getMajorTickSpacing() {
        return TICKS_PER_MEASURE / (2 << beatDivision);
    }

    private void onTempoChange(final float bpm) {
        for (final TempoListener tempoListener : tempoListeners) {
            tempoListener.onTempoChange(bpm);
        }
    }

    @Override
    public void onCreate(MidiNote note) {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            for (MidiNoteListener listener : midiNoteListeners) {
                listener.onCreate(note);
            }
        }
    }

    @Override
    public void onDestroy(MidiNote note) {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            for (MidiNoteListener listener : midiNoteListeners) {
                listener.onDestroy(note);
            }
        }
    }

    @Override
    public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
                       int endNoteValue, long endOnTick, long endOffTick) {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            for (MidiNoteListener listener : midiNoteListeners) {
                listener.onMove(note, beginNoteValue, beginOnTick, beginOffTick, endNoteValue,
                        endOnTick, endOffTick);
            }
        }
    }

    @Override
    public void onSelectStateChange(MidiNote note) {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            for (MidiNoteListener listener : midiNoteListeners) {
                listener.onSelectStateChange(note);
            }
        }
    }

    @Override
    public void beforeLevelChange(MidiNote note) {
        midiNotesEventManager.beforeLevelChange(note);
    }

    @Override
    public void onLevelChange(MidiNote note, LevelType type) {
        for (MidiNoteListener listener : midiNoteListeners) {
            listener.onLevelChange(note, type);
        }
    }

    private void notifyLoopWindowChanged() {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            setLoopTicksNative(loopBeginTick, loopEndTick);
            context.getTrackManager().updateAllTrackNextNotes();
            for (LoopWindowListener listener : loopChangeListeners) {
                listener.onLoopWindowChange(loopBeginTick, loopEndTick);
            }
        }
    }

    public native long getCurrTick();

    private native void setNativeMSPT(long MSPT);

    private native void setLoopTicksNative(long loopBeginTick, long loopEndTick);
}
