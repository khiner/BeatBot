package com.odang.beatbot.event.effect;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.event.Executable;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.ui.view.View;

public abstract class EffectEvent extends Executable {
    protected int trackId, effectPosition;

    public EffectEvent() {
    }

    public EffectEvent(int trackId, int effectPosition) {
        this.trackId = trackId;
        this.effectPosition = effectPosition;
    }

    protected BaseTrack getTrack() {
        return View.context.getTrackManager().getBaseTrackById(trackId);
    }

    protected Effect getEffect() {
        return getTrack().findEffectByPosition(effectPosition);
    }
}
