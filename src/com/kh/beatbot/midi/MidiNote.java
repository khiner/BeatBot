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
		NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0, noteOn.getNoteValue(), 100);
		NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0, noteOff.getNoteValue(), 100);
		return new MidiNote(noteOnCopy, noteOffCopy);
	}
	
	public MidiEvent getOnEvent() {
		return noteOn;
	}
	
	public long getOnTick() {
		return noteOn.getTick();
	}
	
	public long getOffTick() {
		return noteOff.getTick();
	}

	public int getNote() {
		return noteOn.getNoteValue();
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
		this.noteOn.setNoteValue(note);
		this.noteOff.setNoteValue(note);
	}	
	
	public long getNoteLength() {
		return noteOff.getTick() - noteOn.getTick();
	}
}
