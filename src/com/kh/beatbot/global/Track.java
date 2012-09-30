package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect;

public class Track {
	private int id;
	private Instrument instrument;
	public List<Effect> effects;
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public float[][] adsrPoints;
	public float sampleLoopBegin = 0;
	public float sampleLoopEnd = 0;

	public Track(int id, Instrument instrument) {
		this.id = id;
		this.instrument = instrument;
		effects = new ArrayList<Effect>();
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

	/** Wrappers around native methods **/
	// set play mode to reverse
	public void setReverse(boolean reverse) {
		
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
		setPrimaryPan(id, volume);
	}

	public void setPrimaryPitch(float pitch) {
		this.pitch = pitch;
		setPrimaryPitch(id, volume);
	}

	public void setAdsrOn(boolean on) {
		setAdsrOn(id, on);
	}

	// set the native adsr point. x and y range from 0 to 1
	public void setAdsrPoint(int adsrPointNum, float x, float y) {
		setAdsrPoint(id, adsrPointNum, x, y);
	}
	
	public void setSampleBytes(byte[] bytes) {
		setSampleBytes(id, bytes);
	}

	public static native void setReverse(int trackId, boolean reverse);

	public static native float[] normalize(int trackId);

	public static native void setPrimaryVolume(int trackId, float volume);

	public static native void setPrimaryPan(int trackId, float pan);

	public static native void setPrimaryPitch(int trackId, float pitch);

	public static native void setAdsrOn(int trackId, boolean on);
	
	public native void setAdsrPoint(int trackId, int adsrPointNum, float x,
			float y);

	public static native void setSampleBytes(int trackId, byte[] bytes);
}
