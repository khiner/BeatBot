package com.kh.beatbot.event.effect;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Crush;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.file.ProjectFile;

public class EffectCreateEvent extends EffectEvent implements Stateful, Executable {
	final String effectName;
	final String serializedEffect;

	public EffectCreateEvent(int trackId, Effect effect) {
		super(trackId, effect.getPosition());
		this.effectName = effect.getName();
		this.serializedEffect = ProjectFile.effectToJson(effect);
	}

	public EffectCreateEvent(int trackId, int effectPosition, String effectName) {
		super(trackId, effectPosition);
		this.effectName = effectName;
		this.serializedEffect = null;
	}

	@Override
	public void undo() {
		new EffectDestroyEvent(trackId, effectPosition).apply();
	}

	@Override
	public void doExecute() {
		Effect effect = serializedEffect != null ? ProjectFile.effectFromJson(serializedEffect)
				: createEffect();
		getTrack().addEffect(effect);
	}

	private Effect createEffect() {
		if (effectName.equals(Crush.NAME))
			return new Crush(trackId, effectPosition);
		else if (effectName.equals(Chorus.NAME))
			return new Chorus(trackId, effectPosition);
		else if (effectName.equals(Delay.NAME))
			return new Delay(trackId, effectPosition);
		else if (effectName.equals(Flanger.NAME))
			return new Flanger(trackId, effectPosition);
		else if (effectName.equals(Filter.NAME))
			return new Filter(trackId, effectPosition);
		else if (effectName.equals(Reverb.NAME))
			return new Reverb(trackId, effectPosition);
		else if (effectName.equals(Tremolo.NAME))
			return new Tremolo(trackId, effectPosition);
		return null;
	}
}
