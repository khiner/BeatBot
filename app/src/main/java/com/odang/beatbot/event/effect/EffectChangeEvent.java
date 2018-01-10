package com.odang.beatbot.event.effect;


public class EffectChangeEvent extends EffectEvent {
    final String initialEffectName, endEffectName;

    public EffectChangeEvent(int trackId, int effectPosition, String initialEffectName,
                             String endEffectName) {
        super(trackId, effectPosition);
        this.initialEffectName = initialEffectName;
        this.endEffectName = endEffectName;
    }

    @Override
    public void undo() {
        new EffectChangeEvent(trackId, effectPosition, endEffectName, initialEffectName).apply();
    }

    @Override
    public boolean doExecute() {
        new EffectDestroyEvent(trackId, effectPosition).apply();
        new EffectCreateEvent(trackId, effectPosition, endEffectName).apply();
        return true;
    }
}
