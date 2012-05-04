package com.kh.beatbot.midi;

import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;

public class MidiNote {
	NoteOn noteOn;
	NoteOff noteOff;

	public MidiNote(NoteOn noteOn, NoteOff noteOff) {
		this.noteOn = noteOn;
		this.noteOff = noteOff;
	}
	
	public MidiNote getCopy() {
		NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0, noteOn.getNoteValue(), noteOn.getVelocity());
		NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0, noteOff.getNoteValue(), noteOff.getVelocity());
		return new MidiNote(noteOnCopy, noteOffCopy);
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
	
	public int getVelocity() {
		return noteOn.getVelocity();
	}
	
	public void setVelocity(int velocity) {
		if (velocity >= 0 && velocity <= 100) {
			noteOn.setVelocity(velocity);
			noteOff.setVelocity(velocity);
		}
	}
	
	public void setOnTick(long onTick) {
		if (onTick >= 0)
			noteOn.setTick(onTick);
		else
			noteOn.setTick(0);
	}
	
	public void setOffTick(long offTick) {
		if (offTick > getOnTick())
			this.noteOff.setTick(offTick);
	}
	
	public void setNote(int note) {
		if (note < 0)
			return;
		this.noteOn.setNoteValue(note);
		this.noteOff.setNoteValue(note);
	}	
	
	public long getNoteLength() {
		return noteOff.getTick() - noteOn.getTick();
	}
}
