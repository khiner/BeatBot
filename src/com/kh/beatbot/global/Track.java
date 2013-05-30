package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.helper.TickWindowHelper;

public class Track extends BaseTrack {

	private Instrument instrument;

	private boolean adsrEnabled = false, reverse = false;
	private List<MidiNote> notes = new ArrayList<MidiNote>();
	private List<SampleFile> sampleFiles = new ArrayList<SampleFile>();

	private SampleFile currSampleFile;
	public ADSR adsr;

	public Track(int id) {
		super(id);
		this.currSampleFile = null;
		this.adsr = new ADSR(id);
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
		this.instrument = instrument;
		// if we already have this sample in our list, reuse it
		// otherwise, create a new one and add it to the list
		String fileName = instrument.getFullPath(sampleNum);
		SampleFile sampleFile = findSampleFile(fileName);
		if (sampleFile == null) { // haven't created sample file yet
			currSampleFile = instrument.createSampleFile(sampleNum);
			sampleFiles.add(currSampleFile);
		} else if (!sampleFile.equals(currSampleFile)) {
			currSampleFile = sampleFile;
		} else {
			return; // same file, nothing to do
		}
		setSampleNum(sampleNum);
		updateLoopWindow();
		GlobalVars.mainPage.pageSelectGroup.notifyTrackChanged();
	}

	private SampleFile findSampleFile(String fileName) {
		for (SampleFile sampleFile : sampleFiles) {
			if (sampleFile.getFullPath().equals(fileName)) {
				return sampleFile;
			}
		}
		return null;
	}

	public float getLoopBeginSample() {
		return getCurrSampleFile().getLoopBeginSample();
	}

	public float getLoopEndSample() {
		return getCurrSampleFile().getLoopEndSample();
	}

	public float getNumSamples() {
		return getCurrSampleFile().getNumSamples();
	}

	public void setLoopBeginSample(float loopBeginSample) {
		getCurrSampleFile().setLoopBeginSample(loopBeginSample);
		updateLoopWindow();
	}

	public void setLoopEndSample(float loopEndSample) {
		getCurrSampleFile().setLoopEndSample(loopEndSample);
		updateLoopWindow();
	}

	public String getCurrSampleName() {
		return currSampleFile.getName().replace(".wav", "");
	}

	public void setCurrSampleName(String name) {
		currSampleFile.renameTo(instrument.getBasePath() + name + ".wav");
	}

	public String getCurrSamplePath() {
		return currSampleFile.getFullPath();
	}

	public SampleFile getCurrSampleFile() {
		return currSampleFile;
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

	private void updateLoopWindow() {
		setTrackLoopWindow(id, (long) getLoopBeginSample(),
				(long) getLoopEndSample());
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
		setSample(id, getCurrSamplePath());
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
