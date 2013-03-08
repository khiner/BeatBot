package com.kh.beatbot.midi;

import java.nio.FloatBuffer;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;

public class MidiNote implements Comparable<MidiNote> {
	FloatBuffer vb; // vertex buffer for drawing
	NoteOn noteOn;
	NoteOff noteOff;
	boolean selected = false;
	boolean touched = false;

	public MidiNote(NoteOn noteOn, NoteOff noteOff) {
		this.noteOn = noteOn;
		this.noteOff = noteOff;
		updateVb();
	}

	public void setVb(FloatBuffer vb) {
		this.vb = vb;
	}
	
	public FloatBuffer getVb() {
		return vb;
	}
	
	private void updateVb() {
		GlobalVars.midiGroup.midiView.updateNoteVb(this);
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
		if (onTick >= 0)
			noteOn.setTick(onTick);
		else
			noteOn.setTick(0);
		updateVb();
	}

	public void setOffTick(long offTick) {
		if (offTick > getOnTick())
			this.noteOff.setTick(offTick);
		updateVb();
	}

	public void setNote(int note) {
		if (note < 0)
			return;
		this.noteOn.setNoteValue(note);
		this.noteOff.setNoteValue(note);
		updateVb();
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
		}
		return (int) (this.getOnTick() - otherNote.getOnTick());
	}
}
