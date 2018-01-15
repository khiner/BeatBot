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
        params.add(new Param(0, "Reverb Vol").withLevel(0.5f));
        params.add(new Param(1, "Room Size").withLevel(0.5f));
        params.add(new Param(2, "Decay").withLevel(0.5f));
        params.add(new Param(3, "Density").withLevel(0.5f));
        params.add(new Param(4, "Pre-delay").withLevel(0.5f));
        params.add(new Param(5, "Early/Late Mix").withLevel(0.5f));
        params.add(new Param(6, "Damping").withLevel(0.5f));
        params.add(new Param(7, "Brightness").withLevel(0.5f));
    }
}
