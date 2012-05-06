package com.kh.beatbot.midi;

import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.helper.LevelsViewHelper;

public class MidiNote {
	NoteOn noteOn;
	NoteOff noteOff;

	public MidiNote(NoteOn noteOn, NoteOff noteOff) {
		this.noteOn = noteOn;
		this.noteOff = noteOff;
	}
	
	public MidiNote getCopy() {
		NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0, noteOn.getNoteValue(), noteOn.getVelocity(), noteOn.getPan(), noteOn.getPitch());
		NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0, noteOff.getNoteValue(), noteOff.getVelocity(), noteOff.getPan(), noteOn.getPitch());
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
	
	public int getPan() {
		return noteOn.getPan();
	}
	
	public int getPitch() {
		return noteOn.getPitch();
	}
	
	public void setVelocity(int velocity) {
		if (velocity >= 0 && velocity <= 127) {
			noteOn.setVelocity(velocity);
			noteOff.setVelocity(velocity);
		}
	}
	
	public void setPan(int pan) {
		if (pan >= 0 && pan <= 127) {
			noteOn.setPan(pan);
			noteOff.setPan(pan);
		}
	}
	
	public void setPitch(int pitch) {
		if (pitch >= 10 && pitch <= 127) {
			noteOn.setPitch(pitch);
			noteOff.setPitch(pitch);
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
	
	public int getLevel(LevelsViewHelper.LevelMode levelMode) {
		if (levelMode == LevelsViewHelper.LevelMode.VOLUME)
			return noteOn.getVelocity();
		else if (levelMode == LevelsViewHelper.LevelMode.PAN)
			return noteOn.getPan();
		else if (levelMode == LevelsViewHelper.LevelMode.PITCH)
			return noteOn.getPitch();
		
		return 0;
	}
	
	public void setLevel(LevelsViewHelper.LevelMode levelMode, int level) {
		if (levelMode == LevelsViewHelper.LevelMode.VOLUME)
			noteOn.setVelocity(level);
		else if (levelMode == LevelsViewHelper.LevelMode.PAN)
			noteOn.setPan(level);
		else if (levelMode == LevelsViewHelper.LevelMode.PITCH)
			noteOn.setPitch(level);
	}
}
