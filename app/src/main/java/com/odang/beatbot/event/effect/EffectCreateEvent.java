package com.odang.beatbot.event.effect;

import com.odang.beatbot.effect.Chorus;
import com.odang.beatbot.effect.Crush;
import com.odang.beatbot.effect.Delay;
import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Filter;
import com.odang.beatbot.effect.Flanger;
import com.odang.beatbot.effect.Reverb;
import com.odang.beatbot.effect.Tremolo;
import com.odang.beatbot.file.ProjectFile;

public class EffectCreateEvent extends EffectEvent {
    final String effectName;
    final String serializedEffect;

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
    public boolean doExecute() {
        Effect effect = serializedEffect != null ? ProjectFile.effectFromJson(serializedEffect)
                : createEffect();
        if (effect != null) {
            getTrack().addEffect(effect);
            return true;
        } else {
            return false;
        }
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
