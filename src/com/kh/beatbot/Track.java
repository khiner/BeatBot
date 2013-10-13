package com.kh.beatbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.view.TrackButtonRow;
import com.kh.beatbot.ui.view.group.PageSelectGroup;

public class Track extends BaseTrack {

	public static float MIN_LOOP_WINDOW = 32f;

	private boolean adsrEnabled = false, reverse = false, previewing = false;
	private TrackButtonRow buttonRow;
	private List<MidiNote> notes = new ArrayList<MidiNote>();
	private Map<SampleFile, SampleParams> paramsForSample = new HashMap<SampleFile, SampleParams>();
	private SampleFile currSampleFile;

	public ADSR adsr;

	public Track(int id) {
		super(id);
		this.currSampleFile = null;
		this.adsr = new ADSR(this);
		this.buttonRow = new TrackButtonRow(this);
	}

	public void setId(int id) {
		this.id = id;
		for (MidiNote note : notes) {
			note.setNote(id);
		}
	}

	public void select() {
		buttonRow.instrumentButton.trigger(false);
	}

	public TrackButtonRow getButtonRow() {
		return buttonRow;
	}

	public Icon getIcon() {
		return buttonRow.instrumentButton.getIcon();
	}

	public void updateIcon() {
		buttonRow.updateInstrumentIcon();
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

	public void setNoteTicks(MidiNote midiNote, long onTick, long offTick) {
		if (!notes.contains(midiNote)
				|| (midiNote.getOnTick() == onTick && midiNote.getOffTick() == offTick)) {
			return;
		}
		// if we're changing the stop tick on a note that's already playing to a
		// note before the current tick, stop the track
		notifyNoteMoved(midiNote.getOnTick(), midiNote.getOffTick(), onTick,
				offTick);
		// move Java note ticks
		midiNote.setOnTick(onTick);
		midiNote.setOffTick(offTick);
		updateNextNote();
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
					newOnTick = (long) MidiManager.MAX_TICKS * 2;
					newOffTick = (long) MidiManager.MAX_TICKS * 2 + 100;
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
		long currTick = MidiManager.getCurrTick();
		MidiNote nextNote = getNextMidiNote(currTick);
		setNextNote(id, nextNote);
	}

	public MidiNote getNextMidiNote(long currTick) {
		// is there another note starting between the current tick and the end
		// of the loop?
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() >= currTick
					&& midiNote.getOnTick() < MidiManager.getLoopEndTick()) {
				return midiNote;
			}
		}
		// otherwise, get the first note that starts after loop begin
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() >= MidiManager.getLoopBeginTick()) {
				return midiNote;
			}
		}
		return null;
	}

	public Instrument getInstrument() {
		return currSampleFile.getInstrument();
	}

	public void setSample(SampleFile sample) {
		currSampleFile = sample;
		setSample(id, getCurrSamplePath());

		if (!paramsForSample.containsKey(currSampleFile)) {
			paramsForSample.put(currSampleFile, new SampleParams(
					getNumSamples(id)));
		}

		updateLoopWindow();
	}

	public Param getLoopBeginParam() {
		return paramsForSample.get(currSampleFile).loopBeginParam;
	}

	public Param getLoopEndParam() {
		return paramsForSample.get(currSampleFile).loopEndParam;
	}

	public Param getGainParam() {
		return paramsForSample.get(currSampleFile).gainParam;
	}

	public String getCurrSampleName() {
		return currSampleFile.getName().replace(".wav", "");
	}

	public void setCurrSampleName(String name) {
		currSampleFile.renameTo(name + ".wav");
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
		previewing = true;
		previewTrack(id);
	}

	public void stopPreviewing() {
		stopPreviewingTrack(id);
		previewing = false;
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

	public boolean isPreviewing() {
		return previewing;
	}

	private void updateLoopWindow() {
		setTrackLoopWindow(id, (long) getLoopBeginParam().level,
				(long) getLoopEndParam().level);
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

	public float getSample(int sampleIndex, int channel) {
		return getSample(id, sampleIndex, channel);
	}

	public float getCurrentSampleIndex() {
		return getCurrentSampleIndex(id);
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

	public static native void setTrackGain(int trackId, float gain);

	public static native void setSample(int trackId, String sampleName);

	public static native float getSample(int trackId, int sampleIndex,
			int channel);

	public static native float getCurrentSampleIndex(int trackId);

	public static native float getNumSamples(int trackId);

	public native void setNextNote(int trackId, MidiNote midiNote);

	private class SampleParams implements ParamListener {
		private Param loopBeginParam, loopEndParam, gainParam;

		public SampleParams(float numSamples) {
			loopBeginParam = new Param(0, "Begin", "", 0, numSamples);
			loopBeginParam.setFormat("%.0f");
			loopBeginParam.setLevel(0);

			loopEndParam = new Param(1, "End", "", 0, numSamples);
			loopEndParam.setFormat("%.0f");
			loopEndParam.setLevel(1);

			gainParam = new Param(2, "Gain", "", 0, 2);
			gainParam.setLevel(1);

			gainParam.addListener(this);
			loopBeginParam.addListener(this);
			loopEndParam.addListener(this);
		}

		@Override
		public void onParamChanged(Param param) {
			if (param.equals(gainParam)) {
				setTrackGain(id, param.level);
				PageSelectGroup.sampleEditPage.sampleEdit.onParamChanged(param);
			} else {
				float minLoopWindow = loopEndParam
						.getViewLevel(MIN_LOOP_WINDOW);
				loopBeginParam.maxViewLevel = loopEndParam.viewLevel
						- minLoopWindow;
				loopEndParam.minViewLevel = loopBeginParam.viewLevel
						+ minLoopWindow;
				updateLoopWindow();
			}
		}
	}
}
