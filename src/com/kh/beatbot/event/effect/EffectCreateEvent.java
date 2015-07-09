package com.kh.beatbot.event.effect;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Crush;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;

public class EffectCreateEvent extends EffectEvent implements Stateful, Executable {
	final String effectName;

	public EffectCreateEvent(int trackId, int effectPosition, String effectName) {
		super(trackId, effectPosition);
		this.effectName = effectName;
	}

	@Override
	public void apply() {
		doExecute();
	}

	@Override
	public void undo() {
		new EffectDestroyEvent(trackId, effectPosition).apply();
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
	}

	@Override
	public void doExecute() {
		if (effectName.equals(Crush.NAME))
			new Crush(trackId, effectPosition);
		else if (effectName.equals(Chorus.NAME))
			new Chorus(trackId, effectPosition);
		else if (effectName.equals(Delay.NAME))
			new Delay(trackId, effectPosition);
		else if (effectName.equals(Flanger.NAME))
			new Flanger(trackId, effectPosition);
		else if (effectName.equals(Filter.NAME))
			new Filter(trackId, effectPosition);
		else if (effectName.equals(Reverb.NAME))
			new Reverb(trackId, effectPosition);
		else if (effectName.equals(Tremolo.NAME))
			new Tremolo(trackId, effectPosition);
	}
}
