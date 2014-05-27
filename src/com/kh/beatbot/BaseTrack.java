package com.kh.beatbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.TrackManager;

public class BaseTrack {
	protected int id;
	protected List<Effect> effects = new ArrayList<Effect>();
	public Param volumeParam, panParam, pitchParam;

	public BaseTrack(final int id) {
		this.id = id;
		volumeParam = new Param(0, "Volume").withLevel(0.8f);
		panParam = new Param(1, "Pan").withLevel(0.5f);
		pitchParam = new Param(2, "Pitch").withLevel(0.5f);
		volumeParam.addListener(new ParamListener() {
			public void onParamChanged(Param param) {
				setTrackVolume(getId(), param.level);
			}
		});
		panParam.addListener(new ParamListener() {
			public void onParamChanged(Param param) {
				setTrackPan(getId(), param.level);
			}
		});
		pitchParam.addListener(new ParamListener() {
			public void onParamChanged(Param param) {
				setTrackPitch(getId(), param.level);
			}
		});
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
			if (other.getPosition() >= newPosition && other.getPosition() < oldPosition) {
				other.setPosition(other.getPosition() + 1);
			} else if (other.getPosition() <= newPosition && other.getPosition() > oldPosition) {
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

	public Param getVolumeParam() {
		return volumeParam;
	}

	public Param getPanParam() {
		return panParam;
	}

	public Param getPitchParam() {
		return pitchParam;
	}

	public void select() {
		TrackManager.get().onSelect(this);
	}

	public void setLevels(float volume, float pan, float pitch) {
		volumeParam.setLevel(volume);
		panParam.setLevel(pan);
		pitchParam.setLevel(pitch);
		TrackManager.notifyTrackLevelsSetEvent(this);
	}

	public static native void setTrackVolume(int trackId, float volume);

	public static native void setTrackPan(int trackId, float pan);

	public static native void setTrackPitch(int trackId, float pitch);
}
