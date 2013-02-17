package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect;

public class BaseTrack {
	protected int id;
	public List<Effect> effects = new ArrayList<Effect>();
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public GlobalVars.LevelType activeLevelType = GlobalVars.LevelType.VOLUME;

	public BaseTrack(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Effect findEffectByPosition(int position) {
		for (Effect effect : effects) {
			if (effect.getPosition() == position) {
				return effect;
			}
		}
		return null;
	}

	/** Wrappers around native JNI methods **/
	
	public void setVolume(float volume) {
		this.volume = volume;
		setTrackVolume(id, volume);
	}

	public void setPan(float pan) {
		this.pan = pan;
		setTrackPan(id, pan);
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
		setTrackPitch(id, pitch);
	}

	public static native void setTrackVolume(int trackId, float volume);

	public static native void setTrackPan(int trackId, float pan);

	public static native void setTrackPitch(int trackId, float pitch);
}
