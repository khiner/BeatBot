package com.kh.beatbot.midi;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.event.midinotes.MidiNotesEventManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.view.View;

public class MidiNote implements Comparable<MidiNote> {
	// private Rectangle rectangle;
	private NoteOn noteOn;
	private NoteOff noteOff;
	private boolean selected = false, touched = false;

	private transient Rectangle rectangle;

	// while moving notes in the ui, they can overlap, but we keep
	// a memory of the old note ticks while we manipulate the new note ticks
	private long savedOnTick, savedOffTick;

	public MidiNote(NoteOn noteOn, NoteOff noteOff) {
		this.noteOn = noteOn;
		this.noteOff = noteOff;
		savedOnTick = savedOffTick = -1;
	}

	public void create() {
		selected = true;
		notifyCreated();
	}

	public void destroy() {
		notifyDestroyed();
	}

	public void saveTicks() {
		savedOnTick = getOnTick();
		savedOffTick = getOffTick();
	}

	public long getSavedOnTick() {
		return savedOnTick;
	}

	public long getSavedOffTick() {
		return savedOffTick;
	}

	public void finalizeTicks() {
		// erase memory of temp 'old' ticks
		savedOnTick = savedOffTick = -1;
	}

	public MidiNote getCopy() {
		NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0, noteOn.getNoteValue(),
				noteOn.getVelocity(), noteOn.getPan(), noteOn.getPitch());
		NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0, noteOff.getNoteValue(),
				noteOff.getVelocity(), noteOff.getPan(), noteOn.getPitch());
		MidiNote copy = new MidiNote(noteOnCopy, noteOffCopy);
		copy.setSelected(selected);
		copy.setTouched(touched);
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
		notifySelectStateChanged();
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	public void setTicks(long onTick, long offTick) {
		long prevOnTick = getOnTick();
		long prevOffTick = getOffTick();

		noteOn.setTick(onTick >= 0 ? onTick : 0);
		if (offTick > getOnTick()) {
			noteOff.setTick(offTick);
		}

		if (prevOnTick != getOnTick() || prevOffTick != getOffTick()) {
			notifyMoved(getNoteValue(), prevOnTick, prevOffTick, getNoteValue(), getOnTick(),
					getOffTick());
		}
	}

	public void setNote(int note) {
		if (note < 0 || getNoteValue() == note)
			return;

		int prevNoteValue = getNoteValue();
		noteOn.setNoteValue(note);
		noteOff.setNoteValue(note);
		notifyMoved(prevNoteValue, getOnTick(), getOffTick(), getNoteValue(), getOnTick(),
				getOffTick());
	}

	public long getNoteLength() {
		return noteOff.getTick() - noteOn.getTick();
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
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
	}

	@Override
	public int compareTo(MidiNote otherNote) {
		if (this.getNoteValue() != otherNote.getNoteValue()) {
			return this.getNoteValue() - otherNote.getNoteValue();
		} else if (this.getOnTick() != otherNote.getOnTick()) {
			return (int) (this.getOnTick() - otherNote.getOnTick());
		} else if (this.getOffTick() != otherNote.getOffTick()) {
			return (int) (this.getOffTick() - otherNote.getOffTick());
		} else if (this.getVelocity() != otherNote.getVelocity()) {
			return this.getVelocity() - otherNote.getVelocity() < 0 ? -1 : 1;
		} else if (this.getPan() != otherNote.getPan()) {
			return this.getPan() - otherNote.getPan() < 0 ? -1 : 1;
		} else if (this.getPitch() != otherNote.getPitch()) {
			return this.getPitch() - otherNote.getPitch() < 0 ? -1 : 1;
		} else {
			return 0;
		}
	}

	private void notifyCreated() {
		TrackManager.get().onCreate(this);
		View.mainPage.onCreate(this);
	}

	private void notifyDestroyed() {
		TrackManager.get().onDestroy(this);
		View.mainPage.onDestroy(this);
	}

	private void notifyMoved(int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		TrackManager.get().onMove(this, beginNoteValue, beginOnTick, beginOffTick, endNoteValue,
				endOnTick, endOffTick);
		View.mainPage.onMove(this, beginNoteValue, beginOnTick, beginOffTick, endNoteValue,
				endOnTick, endOffTick);
		MidiNotesEventManager.onMove(beginNoteValue, beginOnTick, beginOffTick, endNoteValue,
				endOnTick, endOffTick);
	}

	private void notifySelectStateChanged() {
		TrackManager.get().onSelectStateChange(this);
		View.mainPage.onSelectStateChange(this);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((noteOff == null) ? 0 : noteOff.hashCode());
		result = prime * result + ((noteOn == null) ? 0 : noteOn.hashCode());
		result = prime * result + (int) (savedOffTick ^ (savedOffTick >>> 32));
		result = prime * result + (int) (savedOnTick ^ (savedOnTick >>> 32));
		result = prime * result + (selected ? 1231 : 1237);
		result = prime * result + (touched ? 1231 : 1237);
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
		if (savedOffTick != other.savedOffTick)
			return false;
		if (savedOnTick != other.savedOnTick)
			return false;
		if (selected != other.selected)
			return false;
		if (touched != other.touched)
			return false;
		return true;
	}
}
