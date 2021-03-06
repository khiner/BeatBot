package com.odang.beatbot.midi;

import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.manager.MidiManager;
import com.odang.beatbot.midi.event.MidiEvent;
import com.odang.beatbot.midi.event.NoteOff;
import com.odang.beatbot.midi.event.NoteOn;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.view.View;

public class MidiNote implements Comparable<MidiNote> {
    // private Rectangle rectangle;
    private NoteOn noteOn;
    private NoteOff noteOff;
    private boolean selected = false, touched = false;

    private transient Rectangle rectangle;

    // while moving notes in the ui, they can overlap, but we keep
    // a memory of the old note ticks while we manipulate the new note ticks
    private int savedNoteValue;
    private long savedOnTick, savedOffTick;

    public MidiNote(NoteOn noteOn, NoteOff noteOff) {
        this.noteOn = noteOn;
        this.noteOff = noteOff;
        finalizeTicks();
    }

    public void create() {
        selected = true;
        View.context.getMidiManager().onCreate(this);
    }

    public void destroy() {
        View.context.getMidiManager().onDestroy(this);
        restoreTicks();
    }

    public void saveTicks() {
        savedNoteValue = getNoteValue();
        savedOnTick = getOnTick();
        savedOffTick = getOffTick();
    }

    public int getSavedNoteValue() {
        return savedNoteValue;
    }

    public long getSavedOnTick() {
        return savedOnTick;
    }

    public long getSavedOffTick() {
        return savedOffTick;
    }

    public void finalizeTicks() {
        // erase memory of temp 'old' ticks
        savedNoteValue = -1;
        savedOnTick = savedOffTick = -1;
    }

    public void restoreTicks() {
        if (!isFinalized()) {
            noteOn.setNoteValue(savedNoteValue);
            noteOff.setNoteValue(savedNoteValue);
            noteOn.setTick(savedOnTick);
            noteOff.setTick(savedOffTick);
        }
        finalizeTicks();
    }

    public boolean isFinalized() {
        return savedNoteValue < 0 && savedOnTick < 0 && savedOffTick < 0;
    }

    public boolean isMarkedForDeletion() {
        return getOnTick() > MidiManager.MAX_TICKS;
    }

    public MidiNote getCopy() {
        NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0, noteOn.getNoteValue(),
                noteOn.getVelocity(), noteOn.getPan(), noteOn.getPitch());
        NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0, noteOff.getNoteValue(),
                noteOff.getVelocity(), noteOff.getPan(), noteOn.getPitch());
        MidiNote copy = new MidiNote(noteOnCopy, noteOffCopy);
        copy.setSelected(selected);
        copy.setTouched(touched);
        copy.savedNoteValue = this.savedNoteValue;
        copy.savedOnTick = this.savedOnTick;
        copy.savedOffTick = this.savedOffTick;
        return copy;
    }

    public MidiEvent getOnEvent() {
        return noteOn;
    }

    public MidiEvent getOffEvent() {
        return noteOff;
    }

    public long getOnTick() {
        return noteOn.getTick();
    }

    public long getOffTick() {
        return noteOff.getTick();
    }

    public int getNoteValue() {
        return noteOn.getNoteValue();
    }

    public byte getVelocity() {
        return noteOn.getVelocity();
    }

    public byte getPan() {
        return noteOn.getPan();
    }

    public byte getPitch() {
        return noteOn.getPitch();
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isTouched() {
        return touched;
    }

    public void setSelected(boolean selected) {
        if (this.selected == selected)
            return;
        this.selected = selected;
        View.context.getMidiManager().onSelectStateChange(this);
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }

    public boolean set(int noteValue, long onTick, long offTick) {
        int prevNoteValue = getNoteValue();
        long prevOnTick = getOnTick();
        long prevOffTick = getOffTick();

        doSetTicks(onTick, offTick);
        if (noteValue >= 0) {
            doSetNoteValue(noteValue);
        }

        boolean ticksChanged = prevOnTick != getOnTick() || prevOffTick != getOffTick();
        boolean noteChanged = getNoteValue() != prevNoteValue;
        if (noteChanged || ticksChanged) {
            View.context.getMidiManager().onMove(this, prevNoteValue, prevOnTick, prevOffTick,
                    getNoteValue(), getOnTick(), getOffTick());
        }

        return noteChanged || ticksChanged;
    }

    public boolean setTicks(long onTick, long offTick) {
        long prevOnTick = getOnTick();
        long prevOffTick = getOffTick();

        doSetTicks(onTick, offTick);

        boolean ticksChanged = prevOnTick != getOnTick() || prevOffTick != getOffTick();
        if (ticksChanged) {
            View.context.getMidiManager().onMove(this, getNoteValue(), prevOnTick, prevOffTick,
                    getNoteValue(), getOnTick(), getOffTick());
        }
        return ticksChanged;
    }

    public boolean setNoteValue(int noteValue) {
        if (noteValue < 0)
            return false;

        int prevNoteValue = getNoteValue();
        doSetNoteValue(noteValue);

        boolean noteChanged = getNoteValue() != prevNoteValue;
        if (noteChanged) {
            View.context.getMidiManager().onMove(this, prevNoteValue, getOnTick(), getOffTick(),
                    getNoteValue(), getOnTick(), getOffTick());
        }
        return noteChanged;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Levels getLevels() {
        return new Levels(getLevel(LevelType.VOLUME), getLevel(LevelType.PAN),
                getLevel(LevelType.PITCH));
    }

    public byte getLevel(LevelType levelType) {
        switch (levelType) {
            case VOLUME:
                return noteOn.getVelocity();
            case PAN:
                return noteOn.getPan();
            case PITCH:
                return noteOn.getPitch();
            default:
                return 0;
        }
    }

    public float getLinearLevel(LevelType levelType) {
        return GeneralUtils.byteToLinear(getLevel(levelType));
    }

    public String getLevelDisplay(LevelType levelType) {
        switch (levelType) {
            case VOLUME:
                return String.valueOf(getVelocity());
            case PAN:
                return String.valueOf(getPan());
            case PITCH:
                // for pitch, show transpose amt in semitones
                return String.valueOf((int) getPitch() - Byte.MAX_VALUE / 2 - 1);
        }
        return null;
    }

    public void setLevel(LevelType levelType, byte level) {
        View.context.getMidiManager().beforeLevelChange(this);

        switch (levelType) {
            case VOLUME:
                setVelocity(level);
                break;
            case PAN:
                setPan(level);
                break;
            case PITCH:
                setPitch(level);
                break;
        }

        View.context.getMidiManager().onLevelChange(this, levelType);
    }

    @Override
    public int compareTo(MidiNote other) {
        if (this.getNoteValue() != other.getNoteValue()) {
            return this.getNoteValue() - other.getNoteValue();
        } else if (this.getOnTick() != other.getOnTick()) {
            return (int) (this.getOnTick() - other.getOnTick());
        } else if (this.getOffTick() != other.getOffTick()) {
            return (int) (this.getOffTick() - other.getOffTick());
        } else if (this.getVelocity() != other.getVelocity()) {
            return this.getVelocity() - other.getVelocity() < 0 ? -1 : 1;
        } else if (this.getPan() != other.getPan()) {
            return this.getPan() - other.getPan() < 0 ? -1 : 1;
        } else if (this.getPitch() != other.getPitch()) {
            return this.getPitch() - other.getPitch() < 0 ? -1 : 1;
        } else {
            return 0;
        }
    }

    private void setVelocity(byte velocity) {
        noteOn.setVelocity(velocity);
        noteOff.setVelocity(velocity);
    }

    private void setPan(byte pan) {
        noteOn.setPan(pan);
        noteOff.setPan(pan);
    }

    private void setPitch(byte pitch) {
        noteOn.setPitch(pitch);
        noteOff.setPitch(pitch);
    }

    private void doSetTicks(long onTick, long offTick) {
        noteOn.setTick(onTick >= 0 ? onTick : 0);
        if (offTick > getOnTick()) {
            noteOff.setTick(offTick);
        }
    }

    private void doSetNoteValue(int noteValue) {
        noteOn.setNoteValue(noteValue);
        noteOff.setNoteValue(noteValue);
    }

    public class Levels {
        public byte velocity, pan, pitch;

        public Levels(byte velocity, byte pan, byte pitch) {
            this.velocity = velocity;
            this.pan = pan;
            this.pitch = pitch;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + getNoteValue();
        result = prime * result + (int) getOnTick();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MidiNote other = (MidiNote) obj;
        if (noteOff == null) {
            if (other.noteOff != null)
                return false;
        } else if (!noteOff.equals(other.noteOff))
            return false;
        if (noteOn == null) {
            if (other.noteOn != null)
                return false;
        } else if (!noteOn.equals(other.noteOn))
            return false;
        if (savedNoteValue != other.getSavedNoteValue())
            return false;
        if (savedOnTick != other.getSavedOnTick())
            return false;
        if (savedOffTick != other.getSavedOffTick())
            return false;
        return true;
    }
}
