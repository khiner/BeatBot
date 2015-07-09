package com.kh.beatbot.track;

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
	public Param volumeParam, panParam, pitchStepParam, pitchCentParam;

	public BaseTrack() {
	}

	public BaseTrack(final int id) {
		this.id = id;
		volumeParam = new Param(0, "Vol").withUnits("Db").withLevel(Param.dbToView(0));
		panParam = new Param(1, "Pan").scale(2).add(-1).withLevel(.5f);
		pitchStepParam = new Param(2, "Pit").scale(96).add(-48).withLevel(.5f).snap()
				.withUnits("st");
		pitchCentParam = new Param(3, "Cent").add(-.5f).withLevel(.5f).withUnits("cent");

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
		pitchStepParam.addListener(new ParamListener() {
			public void onParamChanged(Param param) {
				setTrackPitch(getId(), param.level + pitchCentParam.level);
			}
		});
		pitchCentParam.addListener(new ParamListener() {
			public void onParamChanged(Param param) {
				setTrackPitch(getId(), pitchStepParam.level + param.level);
			}
		});
	}

	public int getId() {
		return id;
	}

	public List<Effect> getEffects() {
		return effects;
	}

	public Effect getEffectByPosition(int position) {
		return effects.get(position);
	}

	public void addEffect(Effect effect) {
		effects.add(effect);
		TrackManager.get().onEffectCreate(this, effect);
	}

	public void removeEffect(Effect effect) {
		effects.remove(effect);
		TrackManager.get().onEffectDestroy(this, effect);
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
		TrackManager.get().onEffectOrderChange(this, oldPosition, newPosition);
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
		return pitchStepParam;
	}

	public Param getPitchCentParam() {
		return pitchCentParam;
	}

	public void select() {
		TrackManager.get().onSelect(this);
	}

	public void setLevels(float volume, float pan, float pitchStep, float pitchCent) {
		TrackManager.notifyTrackLevelsSetEvent(this);

		volumeParam.setLevel(volume);
		panParam.setLevel(pan);
		pitchStepParam.setLevel(pitchStep);
		pitchCentParam.setLevel(pitchCent);
	}

	public static native void setTrackVolume(int trackId, float volume);

	public static native void setTrackPan(int trackId, float pan);

	public static native void setTrackPitch(int trackId, float pitch);
}
