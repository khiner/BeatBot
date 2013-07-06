package com.kh.beatbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.LevelType;

public class BaseTrack {
	protected int id;
	protected List<Effect> effects = new ArrayList<Effect>();
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public LevelType activeLevelType = LevelType.VOLUME;

	public BaseTrack(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void addEffect(Effect effect) {
		effects.add(effect);	
	}
	
	public void removeEffect(Effect effect) {
		effects.remove(effect);
	}
	
	public void moveEffect(int oldPosition, int newPosition) {
		Effect effect = findEffectByPosition(oldPosition);
		if (effect != null) {
			effect.setPosition(newPosition);
		}
		for (Effect other : effects) {
			if (other.equals(effect))
				continue;
			if (other.getPosition() >= newPosition
					&& other.getPosition() < oldPosition) {
				other.setPosition(other.getPosition() + 1);
			} else if (other.getPosition() <= newPosition
					&& other.getPosition() > oldPosition) {
				other.setPosition(other.getPosition() - 1);
			}
		}
		Collections.sort(effects);

		Effect.setEffectPosition(id, oldPosition, newPosition);
	}
	
	public void quantizeEffectParams() {
		for (Effect effect : effects) {
			effect.quantizeParams();
		}
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
