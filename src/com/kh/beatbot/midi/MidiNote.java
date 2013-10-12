package com.kh.beatbot.midi;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.ui.mesh.Rectangle;
import com.kh.beatbot.ui.view.page.Page;

public class MidiNote implements Comparable<MidiNote> {
	public static final int BORDER_WIDTH = 2;
	
	Rectangle rectangle; // rectangle for drawing
	NoteOn noteOn;
	NoteOff noteOff;
	boolean selected = false, touched = false;

	// while moving notes in the ui, they can overlap, but we keep 
	// a memory of the old note ticks while we manipulate the new note ticks
	long savedOnTick, savedOffTick;
	
	public MidiNote(NoteOn noteOn, NoteOff noteOff) {
		this.noteOn = noteOn;
		this.noteOff = noteOff;
		savedOnTick = savedOffTick = -1;
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
	
	public Rectangle getRectangle() {
		return rectangle;
	}
	
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	
	private void updateView() {
		Page.mainPage.midiView.updateNoteView(this);
	}
	
	public MidiNote getCopy() {
		NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0,
				noteOn.getNoteValue(), noteOn.getVelocity(), noteOn.getPan(),
				noteOn.getPitch());
		NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0,
				noteOff.getNoteValue(), noteOff.getVelocity(),
				noteOff.getPan(), noteOn.getPitch());
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

	public float getVelocity() {
		return noteOn.getVelocity();
	}

	public float getPan() {
		return noteOn.getPan();
	}

	public float getPitch() {
		return noteOn.getPitch();
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isTouched() {
		return touched;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		updateView(); // color change
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	public void setVelocity(float velocity) {
		velocity = clipLevel(velocity);
		noteOn.setVelocity(velocity);
		noteOff.setVelocity(velocity);
	}

	public void setPan(float pan) {
		pan = clipLevel(pan);
		noteOn.setPan(pan);
		noteOff.setPan(pan);
	}

	public void setPitch(float pitch) {
		pitch = clipLevel(pitch);
		noteOn.setPitch(pitch);
		noteOff.setPitch(pitch);
	}

	// clip the level to be within the min/max allowed
	// (0-1)
	private float clipLevel(float level) {
		if (level < 0)
			return 0;
		if (level > 1)
			return 1;
		return level;
	}

	public void setOnTick(long onTick) {
		noteOn.setTick(onTick >= 0 ? onTick : 0);
		updateView();
	}

	public void setOffTick(long offTick) {
		if (offTick > getOnTick())
			this.noteOff.setTick(offTick);
		updateView();
	}

	public void setNote(int note) {
		if (note < 0)
			return;
		this.noteOn.setNoteValue(note);
		this.noteOff.setNoteValue(note);
		updateView();
	}

	public long getNoteLength() {
		return noteOff.getTick() - noteOn.getTick();
	}

	public float getLevel(LevelType levelType) {
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

	public void setLevel(LevelType levelType, float level) {
		float clippedLevel = clipLevel(level);
		switch (levelType) {
		case VOLUME:
			setVelocity(clippedLevel);
			break;
		case PAN:
			setPan(clippedLevel);
			break;
		case PITCH:
			setPitch(clippedLevel);
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
		} else {
			return this.getPitch() - otherNote.getPitch() < 0 ? -1 : 1; 
		}
	}
}
