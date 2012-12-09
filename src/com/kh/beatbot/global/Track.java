package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.midi.MidiNote;

public class Track {
	private int id;
	private Instrument instrument;
	public List<MidiNote> notes = new ArrayList<MidiNote>();
	public List<Effect> effects = new ArrayList<Effect>();
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public float[][] adsrPoints;
	public float sampleLoopBegin = 0;
	public float sampleLoopEnd = 0;

	public Track(int id, Instrument instrument) {
		this.id = id;
		this.instrument = instrument;
		initDefaultAdsrPoints();
	}

	public int getId() {
		return id;
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

	public Effect findEffectById(int effectId) {
		for (Effect effect : effects) {
			if (effect.getId() == effectId) {
				return effect;
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

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	/** Wrappers around native JNI methods **/
	public void disarm() {
		disarmTrack(id);
	}

	public void play() {
		playTrack(id);
	}

	public void stop() {
		stopTrack(id);
	}

	public void mute(boolean mute) {
		muteTrack(id, mute);
	}

	public void solo(boolean solo) {
		soloTrack(id, solo);
	}

	public void arm() {
		armTrack(id);
	}

	public void toggleLooping() {
		toggleTrackLooping(id);
	}

	public boolean isLooping() {
		return isTrackLooping(id);
	}

	public void setLoopWindow(long loopBegin, long loopEnd) {
		setTrackLoopWindow(id, loopBegin, loopEnd);
	}

	// set play mode to reverse
	public void setReverse(boolean reverse) {
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
		setAdsrOn(id, on);
	}

	// set the native adsr point. x and y range from 0 to 1
	public void setAdsrPoint(int adsrPointNum, float x, float y) {
		setAdsrPoint(id, adsrPointNum, x, y);
	}

	public void setSample(String sampleName) {
		setSample(id, sampleName);
	}

	public static native void armTrack(int trackNum);

	public static native void toggleTrackLooping(int trackNum);

	public static native boolean isTrackLooping(int trackNum);

	public static native void setTrackLoopWindow(int trackNum, long loopBegin,
			long loopEnd);

	public static native void disarmTrack(int trackNum);

	public static native void playTrack(int trackNum);

	public static native void stopTrack(int trackNum);

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
}
