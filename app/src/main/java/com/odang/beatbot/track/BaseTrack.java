package com.odang.beatbot.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.ParamListener;
import com.odang.beatbot.ui.view.View;

public class BaseTrack {
	public static final String MASTER_TRACK_NAME = "Master";

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
			public void onParamChange(Param param) {
				setTrackVolume(getId(), param.level);
			}
		});
		panParam.addListener(new ParamListener() {
			public void onParamChange(Param param) {
				setTrackPan(getId(), param.level);
			}
		});
		pitchStepParam.addListener(new ParamListener() {
			public void onParamChange(Param param) {
				setTrackPitch(getId(), param.level + pitchCentParam.level);
			}
		});
		pitchCentParam.addListener(new ParamListener() {
			public void onParamChange(Param param) {
				setTrackPitch(getId(), pitchStepParam.level + param.level);
			}
		});
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return MASTER_TRACK_NAME;
	}

	public String getFormattedName() {
		return getName();
	}

	public List<Effect> getEffects() {
		return effects;
	}

	public Effect getEffectByPosition(int position) {
		return effects.get(position);
	}

	public void addEffect(Effect effect) {
		effects.add(effect);
		View.context.getTrackManager().onEffectCreate(this, effect);
	}

	public void removeEffect(Effect effect) {
		effects.remove(effect);
		View.context.getTrackManager().onEffectDestroy(this, effect);
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
		View.context.getTrackManager().onEffectOrderChange(this, oldPosition, newPosition);
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
		View.context.getTrackManager().onSelect(this);
	}

	public void setLevels(float volume, float pan, float pitchStep, float pitchCent) {
		View.context.getTrackManager().notifyTrackLevelsSetEvent(this);

		volumeParam.setLevel(volume);
		panParam.setLevel(pan);
		pitchStepParam.setLevel(pitchStep);
		pitchCentParam.setLevel(pitchCent);
	}

	private native void setTrackVolume(int trackId, float volume);

	private native void setTrackPan(int trackId, float pan);

	private native void setTrackPitch(int trackId, float pitchSteps);
}
