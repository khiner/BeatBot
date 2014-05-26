package com.kh.beatbot.midi;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.view.View;

public class MidiNote implements Comparable<MidiNote> {
	private Track track;
	private Rectangle rectangle;
	private NoteOn noteOn;
	private NoteOff noteOff;
	private boolean selected = false, touched = false;

	// order matters - notify tracks of deletion before UI
	private List<MidiNoteListener> listeners = new ArrayList<MidiNoteListener>();
	// while moving notes in the ui, they can overlap, but we keep
	// a memory of the old note ticks while we manipulate the new note ticks
	private long savedOnTick, savedOffTick;

	public MidiNote(NoteOn noteOn, NoteOff noteOff) {
		this(noteOn, noteOff, true);
	}

	private MidiNote(NoteOn noteOn, NoteOff noteOff, boolean notifyCreate) {
		this.noteOn = noteOn;
		this.noteOff = noteOff;
		savedOnTick = savedOffTick = -1;
		this.track = TrackManager.getTrack(noteOn.getNoteValue());
		listeners.add(track);
		listeners.add(View.mainPage);
	}

	public void create() {
		selected = true;
		notifyCreated();
		notifyMoved();
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

	public Rectangle getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}

	public MidiNote getCopy() {
		NoteOn noteOnCopy = new NoteOn(noteOn.getTick(), 0, noteOn.getNoteValue(),
				noteOn.getVelocity(), noteOn.getPan(), noteOn.getPitch());
		NoteOff noteOffCopy = new NoteOff(noteOff.getTick(), 0, noteOff.getNoteValue(),
				noteOff.getVelocity(), noteOff.getPan(), noteOn.getPitch());
		MidiNote copy = new MidiNote(noteOnCopy, noteOffCopy, false);
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
		if (this.selected == selected)
			return;
		this.selected = selected;
		notifySelectStateChanged();
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	public void setVelocity(float velocity) {
		velocity = GeneralUtils.clipToUnit(velocity);
		noteOn.setVelocity(velocity);
		noteOff.setVelocity(velocity);
	}

	public void setPan(float pan) {
		pan = GeneralUtils.clipToUnit(pan);
		noteOn.setPan(pan);
		noteOff.setPan(pan);
	}

	public void setPitch(float pitch) {
		pitch = GeneralUtils.clipToUnit(pitch);
		noteOn.setPitch(pitch);
		noteOff.setPitch(pitch);
	}

	public void setTicks(long onTick, long offTick) {
		noteOn.setTick(onTick >= 0 ? onTick : 0);
		if (offTick > getOnTick()) {
			noteOff.setTick(offTick);
		}
		notifyMoved();
	}

	public void setNote(int note) {
		if (note < 0 || noteOn.getNoteValue() == note)
			return;

		noteOn.setNoteValue(note);
		noteOff.setNoteValue(note);
		setTrack(TrackManager.getTrack(note));
		notifyMoved();
	}

	private void setTrack(Track track) {
		if (null == track || track.equals(this.track))
			return;
		if (null != this.track) { 
			this.track.onDestroy(this);
			listeners.remove(this.track);
		}

		this.track = track;
		listeners.add(this.track);
		this.track.onCreate(this);
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
		float clippedLevel = GeneralUtils.clipToUnit(level);
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

	private void notifyCreated() {
		for (MidiNoteListener listener : listeners) {
			listener.onCreate(this);
		}
	}

	private void notifyDestroyed() {
		for (MidiNoteListener listener : listeners) {
			listener.onDestroy(this);
		}
	}

	private void notifyMoved() {
		for (MidiNoteListener listener : listeners) {
			listener.onMove(this);
		}
	}

	private void notifySelectStateChanged() {
		for (MidiNoteListener listener : listeners) {
			listener.onSelectStateChange(this);
		}
	}
}
