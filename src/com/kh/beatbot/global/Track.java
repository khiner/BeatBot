package com.kh.beatbot.global;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class Track {
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

	private int id;
	private Instrument instrument;
	private int currSampleNum = 0;
	private boolean adsrEnabled = false, reverse = false;
	private List<MidiNote> notes = new ArrayList<MidiNote>();
	public List<Effect> effects = new ArrayList<Effect>();
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public float[][] adsrPoints;
	public GlobalVars.LevelType activeLevelType = GlobalVars.LevelType.VOLUME;

	private Map<Integer, LoopSampleInfo> sampleLoopPoints = new HashMap<Integer, LoopSampleInfo>();

	public Track(int id, Instrument instrument, int sampleNum) {
		this.id = id;
		this.instrument = instrument;
		this.currSampleNum = sampleNum;
		constructLoopPointMap();
		initDefaultAdsrPoints();
	}

	public int getId() {
		return id;
	}

	public void removeNote(MidiNote note) {
		notes.remove(note);
		if (isTrackPlaying(id)) {
			long currTick = Managers.midiManager.getCurrTick();
			if (currTick >= note.getOnTick() && currTick <= note.getOffTick()) {
				stopTrack(id);
			}
		}

		updateNextNote();
	}

	public void addNote(MidiNote note) {
		notes.add(note);
		updateNextNote();
	}

	public void clearNotes() {
		notes.clear();
	}

	public void initDefaultAdsrPoints() {
		adsrPoints = new float[5][2];
		for (int i = 0; i < 5; i++) {
			// x coords
			adsrPoints[i][0] = i / 4f;
		}
		// y coords
		adsrPoints[0][1] = 0;
		adsrPoints[1][1] = 1;
		adsrPoints[2][1] = .60f;
		adsrPoints[3][1] = .60f;
		adsrPoints[4][1] = 0;
	}

	public void updateNextNote() {
		Collections.sort(notes);
		long currTick = Managers.midiManager.getCurrTick();
		MidiNote nextNote = getNextMidiNote(currTick);
		setNextNote(id, nextNote);
	}

	public MidiNote getNextMidiNote(long currTick) {
		for (MidiNote midiNote : notes) {
			if (midiNote.getOffTick() > currTick
					&& midiNote.getOnTick() < MidiManager.loopEndTick) {
				return midiNote;
			}
		}
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() >= MidiManager.loopBeginTick) {
				return midiNote;
			}
		}
		return null;
	}

	public Effect findEffectByPosition(int position) {
		for (Effect effect : effects) {
			if (effect.getPosition() == position) {
				return effect;
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

	public String getSamplePath() {
		return instrument.getSamplePath(currSampleNum);
	}

	public File getSampleFile() {
		return instrument.getSampleFile(currSampleNum);
	}

	private void constructLoopPointMap() {
		sampleLoopPoints.clear();
		for (int sampleNum = 0; sampleNum < instrument.getSampleNames().length; sampleNum++) {
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

	public boolean isPlaying() {
		return isTrackPlaying(id);
	}

	public boolean isLooping() {
		return isTrackLooping(id);
	}

	public boolean isAdsrEnabled() {
		return adsrEnabled;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setLoopWindow(long loopBegin, long loopEnd) {
		setTrackLoopWindow(id, loopBegin, loopEnd);
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

	public void setPrimaryVolume(float volume) {
		this.volume = volume;
		setPrimaryVolume(id, volume);
	}

	public void setPrimaryPan(float pan) {
		this.pan = pan;
		setPrimaryPan(id, pan);
	}

	public void setPrimaryPitch(float pitch) {
		this.pitch = pitch;
		setPrimaryPitch(id, pitch);
	}

	public void setAdsrOn(boolean on) {
		adsrEnabled = on;
		setAdsrOn(id, on);
	}

	// set the native adsr point. x and y range from 0 to 1
	public void setAdsrPoint(int adsrPointNum, float x, float y) {
		setAdsrPoint(id, adsrPointNum, x, y);
	}

	public void setSampleNum(int sampleNum) {
		currSampleNum = sampleNum;
		setSample(id, getSamplePath());
	}

	public static native void toggleTrackLooping(int trackNum);

	public static native boolean isTrackLooping(int trackNum);

	public static native boolean isTrackPlaying(int trackNum);

	public static native void setTrackLoopWindow(int trackNum, long loopBegin,
			long loopEnd);

	public static native void stopTrack(int trackNum);

	public static native void previewTrack(int trackNum);

	public static native void stopPreviewingTrack(int trackNum);

	public static native void muteTrack(int trackNum, boolean mute);

	public static native void soloTrack(int trackNum, boolean solo);

	public static native void setTrackReverse(int trackId, boolean reverse);

	public static native float[] normalize(int trackId);

	public static native void setPrimaryVolume(int trackId, float volume);

	public static native void setPrimaryPan(int trackId, float pan);

	public static native void setPrimaryPitch(int trackId, float pitch);

	public static native void setAdsrOn(int trackId, boolean on);

	public native void setAdsrPoint(int trackId, int adsrPointNum, float x,
			float y);

	public static native void setSample(int trackId, String sampleName);

	public native void setNextNote(int trackId, MidiNote midiNote);
}
