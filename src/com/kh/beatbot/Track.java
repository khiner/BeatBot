package com.kh.beatbot;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.view.TrackButtonRow;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;

public class Track extends BaseTrack implements ParamListener {

	public static float MIN_LOOP_WINDOW = 32f;
	
	private Param loopBeginParam, loopEndParam, gainParam;
	
	private Instrument instrument;
	private TrackButtonRow buttonRow;

	private boolean adsrEnabled = false, reverse = false;
	private List<MidiNote> notes = new ArrayList<MidiNote>();
	private List<SampleFile> sampleFiles = new ArrayList<SampleFile>();

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
		long currTick = MidiManager.getCurrTick();
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
			
			setSampleNum(sampleNum);

			loopBeginParam = new Param(0, "Begin", "", 0, getNumSamples(id));
			loopEndParam = new Param(1, "End", "", 0, getNumSamples(id));
			loopBeginParam.setFormat("%.0f");
			loopEndParam.setFormat("%.0f");
			loopBeginParam.setLevel(0);
			loopEndParam.setLevel(1);
			
			gainParam = new Param(2, "Gain", "", 0, 1);
			gainParam.setLevel(0.5f);
			
		} else if (!sampleFile.equals(currSampleFile)) {
			
			setSampleNum(sampleNum);
			
			gainParam.removeListener(this);
			loopBeginParam.removeListener(this);
			loopEndParam.removeListener(this);
			currSampleFile = sampleFile;
		} else {
			return; // same file, nothing to do
		}
		gainParam.addListener(this);
		loopBeginParam.addListener(this);
		loopEndParam.addListener(this);
	}

	private SampleFile findSampleFile(String fileName) {
		for (SampleFile sampleFile : sampleFiles) {
			if (sampleFile.getFullPath().equals(fileName)) {
				return sampleFile;
			}
		}
		return null;
	}

	public Param getLoopBeginParam() {
		return loopBeginParam;
	}

	public Param getLoopEndParam() {
		return loopEndParam;
	}

	public Param getGainParam() {
		return gainParam;
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

	@Override
	public void onParamChanged(Param param) {
		if (param.equals(getGainParam())) {
			setTrackGain(id, param.level);
		} else {
			float minLoopWindow = getLoopEndParam().getViewLevel(MIN_LOOP_WINDOW);
			getLoopBeginParam().maxViewLevel = getLoopEndParam().viewLevel - minLoopWindow;
			getLoopEndParam().minViewLevel = getLoopBeginParam().viewLevel + minLoopWindow;
			updateLoopWindow();
		}
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

	public void setSampleNum(int sampleNum) {
		setSample(id, getCurrSamplePath());
	}

	public FloatBuffer floatFileToBuffer(View view, long offset,
			long numFloats, int xOffset) throws IOException {
		float spp = Math.min(2, numFloats / view.width);
		float[] outputAry = new float[2 * (int) (view.width * spp)];

		for (int x = 0; x < outputAry.length; x += 2) {
			float percent = (float) x / outputAry.length;
			int dataIndex = (int) (offset + percent * numFloats);
			float sample = getFloatSample(id, dataIndex, 0);
			float y = view.height * (sample + 1) / 2;
			outputAry[x] = percent * view.width + xOffset;
			outputAry[x + 1] = y;
		}
		return View.makeFloatBuffer(outputAry);
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

	public static native float getFloatSample(int trackId, int sampleIndex, int channel);

	public static native float getNumSamples(int trackId);

	public native void setNextNote(int trackId, MidiNote midiNote);
}
