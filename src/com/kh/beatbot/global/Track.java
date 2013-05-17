package com.kh.beatbot.global;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.helper.TickWindowHelper;

public class Track extends BaseTrack {
	class LoopSampleInfo {
		public float loopBeginSample;
		public float loopEndSample;
		public float totalNumSamples;

		LoopSampleInfo(float totalNumSamples) {
			this.loopBeginSample = 0;
			this.loopEndSample = totalNumSamples;
			this.totalNumSamples = totalNumSamples;
		}
	}

	private Instrument instrument;
	private int currSampleNum = 0;
	private boolean adsrEnabled = false, reverse = false;
	private List<MidiNote> notes = new ArrayList<MidiNote>();
	public ADSR adsr;

	private Map<Integer, LoopSampleInfo> sampleLoopPoints = new HashMap<Integer, LoopSampleInfo>();

	public Track(int id, Instrument instrument, int sampleNum) {
		super(id);
		this.instrument = instrument;
		this.currSampleNum = sampleNum;
		this.adsr = new ADSR(id);
		constructLoopPointMap();
	}

	public void removeNote(MidiNote note) {
		notes.remove(note);
		notifyNoteRemoved(id, note.getOnTick(), note.getOffTick());
		updateNextNote();
	}

	public void addNote(MidiNote note) {
		notes.add(note);
		updateNextNote();
	}

	public boolean setNoteTicks(MidiNote midiNote, long onTick, long offTick) {
		if (!notes.contains(midiNote)
				|| (midiNote.getOnTick() == onTick && midiNote.getOffTick() == offTick)) {
			return false;
		}
		// if we're changing the stop tick on a note that's already playing to a
		// note before the current tick, stop the track
		notifyNoteMoved(midiNote.getOnTick(), midiNote.getOffTick(), onTick,
				offTick);
		// move Java note ticks
		midiNote.setOnTick(onTick);
		midiNote.setOffTick(offTick);
		updateNextNote();
		return true;
	}

	public void handleNoteCollisions() {
		for (int i = 0; i < notes.size(); i++) {
			MidiNote note = notes.get(i);
			long newOnTick = note.isSelected() ? note.getOnTick() : note
					.getSavedOnTick();
			long newOffTick = note.isSelected() ? note.getOffTick() : note
					.getSavedOffTick();
			for (int j = 0; j < notes.size(); j++) {
				MidiNote otherNote = notes.get(j);
				if (note.equals(otherNote) || !otherNote.isSelected()) {
					continue;
				}
				// if a selected note begins in the middle of another note,
				// clip the covered note
				if (otherNote.getOnTick() > newOnTick
						&& otherNote.getOnTick() - 1 < newOffTick) {
					newOffTick = otherNote.getOnTick() - 1;
					// otherwise, if a selected note overlaps with the beginning
					// of another note, delete the note
					// (CAN NEVER DELETE SELECTED NOTES THIS WAY!)
				} else if (!note.isSelected()
						&& otherNote.getOnTick() <= newOnTick
						&& otherNote.getOffTick() > newOnTick) {
					// we 'delete' the note temporarily by moving
					// it offscreen, so it won't ever be played or drawn
					newOnTick = (long) TickWindowHelper.MAX_TICKS * 2;
					newOffTick = (long) TickWindowHelper.MAX_TICKS * 2 + 100;
					break;
				}
			}
			setNoteTicks(note, newOnTick, newOffTick);
		}
	}

	public List<MidiNote> getMidiNotes() {
		return notes;
	}

	public void updateNextNote() {
		Collections.sort(notes);
		long currTick = Managers.midiManager.getCurrTick();
		MidiNote nextNote = getNextMidiNote(currTick);
		setNextNote(id, nextNote);
	}

	public MidiNote getNextMidiNote(long currTick) {
		// is there another note starting between the current tick and the end
		// of the loop?
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() >= currTick
					&& midiNote.getOnTick() < MidiManager.loopEndTick) {
				return midiNote;
			}
		}
		// otherwise, get the first note that starts after loop begin
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() >= MidiManager.loopBeginTick) {
				return midiNote;
			}
		}
		return null;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument, int sampleNum) {
		if (this.instrument == instrument && this.currSampleNum == sampleNum)
			return;
		this.instrument = instrument;
		setSampleNum(sampleNum);
		constructLoopPointMap();
		GlobalVars.mainPage.pageSelectGroup.notifyTrackChanged();
	}

	public float getLoopBeginSample() {
		return sampleLoopPoints.get(currSampleNum).loopBeginSample;
	}

	public float getLoopEndSample() {
		return sampleLoopPoints.get(currSampleNum).loopEndSample;
	}

	public float getNumSamples() {
		return sampleLoopPoints.get(currSampleNum).totalNumSamples;
	}

	public void setLoopBeginSample(float loopBeginSample) {
		sampleLoopPoints.get(currSampleNum).loopBeginSample = loopBeginSample;
		setLoopWindow((long) loopBeginSample,
				(long) sampleLoopPoints.get(currSampleNum).loopEndSample);
	}

	public void setLoopEndSample(float loopEndSample) {
		sampleLoopPoints.get(currSampleNum).loopEndSample = loopEndSample;
		setLoopWindow(
				(long) sampleLoopPoints.get(currSampleNum).loopBeginSample,
				(long) loopEndSample);
	}

	public String getSampleName() {
		return instrument.getSampleName(currSampleNum);
	}

	public String getCurrSampleName() {
		return instrument.getSampleName(currSampleNum).replace(".bb", "");
	}
	
	public void setCurrSampleName(String name) {
		instrument.setSampleName(currSampleNum, name + ".bb");
	}
	
	public String getSamplePath() {
		return instrument.getSamplePath(currSampleNum);
	}

	public File getSampleFile() {
		return instrument.getSampleFile(currSampleNum);
	}

	private void constructLoopPointMap() {
		sampleLoopPoints.clear();
		for (int sampleNum = 0; sampleNum < instrument.getChildNames().length; sampleNum++) {
			long numSamples = instrument.getNumSamples(sampleNum);
			sampleLoopPoints.put(sampleNum, new LoopSampleInfo(numSamples));
		}
	}

	/** Wrappers around native JNI methods **/

	public void stop() {
		stopTrack(id);
	}

	public void preview() {
		previewTrack(id);
	}

	public void stopPreviewing() {
		stopPreviewingTrack(id);
	}

	public void mute(boolean mute) {
		muteTrack(id, mute);
	}

	public void solo(boolean solo) {
		soloTrack(id, solo);
	}

	public void toggleLooping() {
		toggleTrackLooping(id);
	}

	public boolean isLooping() {
		return isTrackLooping(id);
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setLoopWindow(long loopBegin, long loopEnd) {
		setTrackLoopWindow(id, loopBegin, loopEnd);
	}

	public void notifyNoteMoved(long oldNoteOn, long oldNoteOff,
			long newNoteOn, long newNoteOff) {
		notifyNoteMoved(id, oldNoteOn, oldNoteOff, newNoteOn, newNoteOff);
	}

	// set play mode to reverse
	public void setReverse(boolean reverse) {
		this.reverse = reverse;
		setTrackReverse(id, reverse);
	}

	// scale all samples so that the sample with the highest amplitude is at 1
	public float[] normalize() {
		return normalize(id);
	}

	public void setSampleNum(int sampleNum) {
		currSampleNum = sampleNum;
		setSample(id, getSamplePath());
	}

	public static native void toggleTrackLooping(int trackNum);

	public static native boolean isTrackLooping(int trackNum);

	public static native void notifyNoteMoved(int trackNum, long oldNoteOn,
			long oldNoteOff, long newNoteOn, long newNoteOff);

	public static native void notifyNoteRemoved(int trackNum, long noteOn,
			long noteOff);

	public static native void setTrackLoopWindow(int trackNum, long loopBegin,
			long loopEnd);

	public static native void stopTrack(int trackNum);

	public static native void previewTrack(int trackNum);

	public static native void stopPreviewingTrack(int trackNum);

	public static native void muteTrack(int trackNum, boolean mute);

	public static native void soloTrack(int trackNum, boolean solo);

	public static native void setTrackReverse(int trackId, boolean reverse);

	public static native float[] normalize(int trackId);

	public static native void setSample(int trackId, String sampleName);

	public native void setNextNote(int trackId, MidiNote midiNote);
}
