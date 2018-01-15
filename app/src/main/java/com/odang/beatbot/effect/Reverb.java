package com.odang.beatbot.effect;

public class Reverb extends Effect {
    public static final String NAME = "Reverb";
    public static final int EFFECT_NUM = 5;

    public Reverb() {
        super();
    }

    public Reverb(int trackId, int position) {
        super(trackId, position);
    }

    public String getName() {
        return NAME;
    }

    public int getId() {
        return EFFECT_NUM;
    }

    @Override
    protected void initParams() {
        params.add(new Param(0, "Room Size").withLevel(0.5f));
        params.add(new Param(1, "Damping").withLevel(0.5f));
        params.add(new Param(2, "Width").withLevel(1.0f));
        params.add(new Param(3, "Wet").withLevel(1.0f));
    }
}
